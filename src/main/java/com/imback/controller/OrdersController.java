package com.imback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.imback.common.BaseContext;
import com.imback.common.Result;
import com.imback.dto.OrdersDto;
import com.imback.entity.AddressBook;
import com.imback.entity.OrderDetail;
import com.imback.entity.Orders;
import com.imback.service.OrderDetailService;
import com.imback.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * PC端分页条件查询
     * @param
     * @return
     */
    @GetMapping("/page")
    public Result<Page<OrdersDto>> page(Integer page, Integer pageSize, String number,
                                        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") String beginTime,
                                        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") String endTime){
        log.info("beginTime={},endTime={}",beginTime,endTime);
        //分页构造器
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        //创建一个空的分页Dto
        Page<OrdersDto> ordersDtoPage = new Page<>();
        //拷贝分页相关数据
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");

        LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
        ordersQueryWrapper.eq(StringUtils.isNotEmpty(number),Orders::getNumber,number);
        ordersQueryWrapper.gt(beginTime!=null,Orders::getOrderTime,beginTime);
        ordersQueryWrapper.lt(endTime!=null,Orders::getOrderTime,endTime);
        ordersQueryWrapper.orderByDesc(Orders::getOrderTime);
        List<Orders> ordersList = ordersService.list(ordersQueryWrapper);
        //遍历ordersList收集成ordersDtoList
        List<OrdersDto> ordersDtoList = ordersList.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //拷贝订单数据
            BeanUtils.copyProperties(item,ordersDto);

            ordersDto.setUserName(item.getConsignee());
            //通过订单Id查询订单明细并拷贝到Dto中
            LambdaQueryWrapper<OrderDetail> orderDetailQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailQueryWrapper.eq(OrderDetail::getOrderId,item.getId());
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailQueryWrapper);
            ordersDto.setOrderDetails(orderDetailList);

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtoList);
        return Result.success(ordersDtoPage);
    }

    /**
     * PC端 派送
     */
    @PutMapping
    public Result<String> distribute(@RequestBody Orders orders){
        log.info("orders={}",orders);
        //修改订单状态为派送
        ordersService.updateById(orders);
        return Result.success("开始派送");
    }

    /**
     * 移动端查询订单明细
     * @return
     */
    @GetMapping("/userPage")
    public Result<Page<OrdersDto>> userPage(Integer page,Integer pageSize){
        //分页构造器
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        //条件
        LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
        ordersQueryWrapper.eq(BaseContext.getCurrentId()!=null,Orders::getUserId,BaseContext.getCurrentId());
        //排序
        ordersQueryWrapper.orderByDesc(Orders::getOrderTime);
        List<Orders> ordersList = ordersService.list(ordersQueryWrapper);
        //先拷贝分页相关的数据
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");

        //遍历ordersList
        List<OrdersDto> ordersDtoList = ordersList.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //把orders数据拷贝到OrdersDto中
            BeanUtils.copyProperties(item , ordersDto);
            //查询每一个订单对应的明细并填充到OrdersDto中
            LambdaQueryWrapper<OrderDetail> orderDetailQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailQueryWrapper.eq(OrderDetail::getOrderId,item.getId());
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailQueryWrapper);
            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtoList);
        return Result.success(ordersDtoPage);
    }


    /**
     * 移动端用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){
        log.info("orders={}",orders);
        ordersService.submit(orders);
        return Result.success("下单成功");
    }
}
