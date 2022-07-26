package com.imback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imback.common.BaseContext;
import com.imback.common.Result;
import com.imback.entity.ShoppingCart;
import com.imback.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 移动端菜品展示
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 查询购物车
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        //根据user_id查询
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseContext.getCurrentId()!=null, ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        return Result.success(shoppingCartList);
    }

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("shoppingCart = {}",shoppingCart);
        //1.设置用户Id ,确定是哪个用户的购物车
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //2.查询当前菜品或套餐是已经存在购物车(存在number+1)
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if(dishId != null){
            //添加的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else{
            //添加的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);

        //判断否已经存在购物车
        if(shoppingCartOne != null){
            //存在购物车  number+1
            shoppingCartOne.setNumber(shoppingCartOne.getNumber()+1);
            shoppingCartService.updateById(shoppingCartOne);
        }else{
            //不存在  保存到购物车
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //为了方便返回值
            shoppingCartOne = shoppingCart;
        }

        return Result.success(shoppingCartOne);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> clean(){
        //根据用户id清空购物车
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseContext.getCurrentId()!=null,ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return Result.success("清空购物车成功");
    }


    @PostMapping("/sub")
    public Result<String> sub(@RequestBody ShoppingCart shoppingCart){
        //判断-1的是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if(dishId!=null){
            //菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            //套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);
        Integer number = shoppingCartOne.getNumber();
        //判断购物车中菜品或套餐的number=1
        if(number > 1){
            //number减1
            shoppingCartOne.setNumber(number-1);
            shoppingCartService.updateById(shoppingCartOne);
        }else{
            //删除菜品或套餐
            shoppingCartService.remove(queryWrapper);
        }

        return Result.success("成功");
    }

}
