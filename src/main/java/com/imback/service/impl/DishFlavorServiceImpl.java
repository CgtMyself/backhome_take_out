package com.imback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imback.dao.DishFlavorMapper;
import com.imback.entity.DishFlavor;
import com.imback.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
