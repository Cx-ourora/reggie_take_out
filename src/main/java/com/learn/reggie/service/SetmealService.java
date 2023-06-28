package com.learn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learn.reggie.dto.SetmealDto;
import com.learn.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    //新增套餐，同时保存套餐和菜品的关联关系
    void saveWithDish(SetmealDto setmealDto);

    //删除套餐，同时删除套餐和菜品的关联关系
    void removeWithDish(List<Long> ids);

    void updateWithDish(SetmealDto setmealDto);

    SetmealDto getByIdWithDish(Long id);
}
