package com.imback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imback.common.BaseContext;
import com.imback.common.CustomException;
import com.imback.dao.OrdersMapper;
import com.imback.entity.*;
import com.imback.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper , Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;
    /**
     * 用户下单
     * @param orders
     */
    @Override
    public void submit(Orders orders) {
        //1.获取用户Id
        Long userId = BaseContext.getCurrentId();
        //2.获取购物车信息
        LambdaQueryWrapper<ShoppingCart> shoppingCartQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartQueryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartQueryWrapper);
        if(shoppingCartList==null || shoppingCartList.size()==0){
            throw new CustomException("购物车为空，不能下单");
        }
        //查询用户信息
        User user = userService.getById(userId);
        //查询默认地址信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if(addressBook == null){
            throw new CustomException("地址信息有误，不能下单");
        }
        //通过IdWorker生成一个订单Id
        long orderId = IdWorker.getId();

        //原子计算  高并发情况下也能精确计算 ,初始值设置为0然后进行累加
        AtomicInteger amount = new AtomicInteger(0);
        //计算付款金额的同时 为订单明细表填充数据
        List<OrderDetail> orderDetails = shoppingCartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            //对金额进行计算 multiply代表的是乘 金额*数量
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());


        //3.向订单表插入数据，一条
        orders.setNumber(String.valueOf(orderId));//订单Id
        orders.setStatus(2); //1待付款，2待派送，3已派送，4已完成，5已取消
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAmount(new BigDecimal(amount.get()));//付款金额
        orders.setUserName(user.getName());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress((addressBook.getProvinceName()==null?"":addressBook.getProvinceName())+
                (addressBook.getCityName()==null?"":addressBook.getCityName())+
                (addressBook.getDistrictName()==null?"":addressBook.getDistrictName())+
                (addressBook.getDetail()==null?"":addressBook.getDetail()));
        this.save(orders);
        //4.向订单明细表插入多条数据
        orderDetailService.saveBatch(orderDetails);
        //5.删除购物车信息
        shoppingCartService.remove(shoppingCartQueryWrapper);
    }
}
