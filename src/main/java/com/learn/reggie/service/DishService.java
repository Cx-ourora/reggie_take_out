package com.learn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learn.reggie.dto.DishDto;
import com.learn.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，操作dish和dish_flavor表
    void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和口味数据
    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dto);

    //根据传过来的id批量或者是单个的删除菜品，并判断是否是启售的
    void deleteByIds(List<Long> ids);

    //菜品批量删除和单个删除，删除时用到deleteByIds方法删除菜品
    boolean deleteInSetmeal(List<Long> ids);


}
