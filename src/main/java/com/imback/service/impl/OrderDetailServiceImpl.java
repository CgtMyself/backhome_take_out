package com.imback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imback.dao.OrderDetailMapper;
import com.imback.entity.OrderDetail;
import com.imback.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper,OrderDetail> implements OrderDetailService {
}
