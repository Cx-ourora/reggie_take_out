package com.learn.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.reggie.dto.DishDto;
import com.learn.reggie.entity.DishFlavor;
import com.learn.reggie.mapper.DishFlavorMapper;
import com.learn.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {

}
