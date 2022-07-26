package com.imback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imback.dao.ShoppingCartMapper;
import com.imback.entity.ShoppingCart;
import com.imback.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
