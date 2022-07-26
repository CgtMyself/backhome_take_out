package com.imback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imback.entity.Orders;

public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);
}
