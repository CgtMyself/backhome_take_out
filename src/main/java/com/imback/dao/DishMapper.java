package com.imback.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imback.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
