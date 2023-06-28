package com.learn.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.reggie.common.CustomException;
import com.learn.reggie.dto.DishDto;
import com.learn.reggie.entity.Dish;
import com.learn.reggie.entity.DishFlavor;
import com.learn.reggie.entity.Setmeal;
import com.learn.reggie.entity.SetmealDish;
import com.learn.reggie.mapper.DishMapper;
import com.learn.reggie.service.DishFlavorService;
import com.learn.reggie.service.DishService;
import com.learn.reggie.service.SetmealDishService;
import com.learn.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增菜品，同时保存口味数据
     * @param dishDto
     */
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();//菜品id

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品信息和口味数据
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询基本信息
        Dish dish = this.getById(id);

        //将查询的基本信息拷贝
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(list);

        return dishDto;
    }

    /**
     * 更新菜品信息和对应的口味信息
     * @param dto
     */
    @Override
    public void updateWithFlavor(DishDto dto) {
        //更新dish表的基本信息
        this.updateById(dto);
        //清理当前菜品对应的口味数据->dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据->dish_flavor表的insert操作
        List<DishFlavor> flavors = dto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //构造条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //先查询该菜品是否在售卖，如果是则抛出业务异常
        queryWrapper.in(ids!=null,Dish::getId,ids);
        List<Dish> list = this.list(queryWrapper);
        for (Dish dish : list) {
            Integer status = dish.getStatus();
            //如果不是在售卖,则可以删除
            if (status == 0){
                this.removeById(dish.getId());
            }else {
                //此时应该回滚,因为可能前面的删除了，但是后面的是正在售卖
                throw new CustomException("删除菜品中有正在售卖菜品,无法全部删除");
            }
        }
    }

    @Override
    @Transactional
    public boolean deleteInSetmeal(List<Long> ids) {
        boolean flag=true;

        //1.根据菜品id在stemeal_dish表中查出哪些套餐包含该菜品
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getDishId,ids);
        List<SetmealDish> SetmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
        //2.如果菜品没有关联套餐，直接删除就行  其实下面这个逻辑可以抽离出来，这里我就不抽离了
        if (SetmealDishList.size() == 0){
            //这个deleteByIds中已经做了菜品启售不能删除的判断力
            this.deleteByIds(ids);
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(DishFlavor::getDishId,ids);
            dishFlavorService.remove(queryWrapper);
            return flag;
        }

        //3.如果菜品有关联套餐，并且该套餐正在售卖，那么不能删除
        //3.1得到与删除菜品关联的套餐id
        ArrayList<Long> Setmeal_idList = new ArrayList<>();
        for (SetmealDish setmealDish : SetmealDishList) {
            Long setmealId = setmealDish.getSetmealId();
            Setmeal_idList.add(setmealId);
        }
        //3.2查询出与删除菜品相关联的套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,Setmeal_idList);
        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);
        //3.3对拿到的所有套餐进行遍历，然后拿到套餐的售卖状态，如果有套餐正在售卖那么删除失败
        for (Setmeal setmeal : setmealList) {
            Integer status = setmeal.getStatus();
            if (status == 1){
                flag=false;
            }
        }

        //3.4要删除的菜品关联的套餐没有在售，可以删除
        //3.5这下面的代码并不一定会执行,因为如果前面的for循环中出现status == 1,那么下面的代码就不会再执行
        this.deleteByIds(ids);
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper);

        return flag;
    }
}
