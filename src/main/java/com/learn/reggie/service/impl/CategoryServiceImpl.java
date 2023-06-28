package com.learn.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.reggie.common.CustomException;
import com.learn.reggie.entity.Category;
import com.learn.reggie.entity.Dish;
import com.learn.reggie.entity.Setmeal;
import com.learn.reggie.mapper.CategoryMapper;
import com.learn.reggie.service.CategoryService;
import com.learn.reggie.service.DishService;
import com.learn.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    /**
     * 根据id删除分类
     * 删除之前进行判断
     * @param id
     */
    @Override
    public void remove(Long id) {

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count = dishService.count(dishLambdaQueryWrapper);
        //判断是否关联菜品，若关联，抛出业务异常
        if (count>0) {
            throw new CustomException("当前分类已关联菜品，无法删除");
        }

        //判断是否关联套餐，若关联，抛出业务异常

        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count1 = setmealService.count(lambdaQueryWrapper);
        if (count1>0) {
            throw new CustomException("当前分类已关联套餐，无法删除");
        }

        //正常删除
        super.removeById(id);
    }
}
