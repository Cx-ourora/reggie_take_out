package com.learn.reggie.dto;

import com.learn.reggie.entity.Setmeal;
import com.learn.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
