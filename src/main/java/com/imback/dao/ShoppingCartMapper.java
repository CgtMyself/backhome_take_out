package com.imback.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imback.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
