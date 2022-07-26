package com.imback.dto;

import com.imback.entity.Dish;
import com.imback.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDishDto extends SetmealDish {
    //图片
    private String image;

    //描述信息
    private String description;
}
