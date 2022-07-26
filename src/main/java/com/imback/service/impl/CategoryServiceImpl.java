package com.imback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imback.common.CustomException;
import com.imback.dao.CategoryMapper;
import com.imback.dao.DishMapper;
import com.imback.entity.Category;
import com.imback.entity.Dish;
import com.imback.entity.Setmeal;
import com.imback.service.CategoryService;
import com.imback.service.DishService;
import com.imback.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    /**
     * 菜品及套餐删除业务逻辑
     */
    @Override
    public void remove(Long id) {
        //查看有没有关联菜品，有的话不能直接删除
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId,id);
        int dishCount = dishService.count(dishQueryWrapper);
        if(dishCount > 0){
            //有菜品不能删，抛出一个自定义业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        //查看有没有关联套餐，有的话不能直接删除
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.eq(Setmeal::getCategoryId,id);
        int setmealCount = setmealService.count(setmealQueryWrapper);
        if(setmealCount > 0){
            //有套餐不能删，抛出一个自定义业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        super.removeById(id);
    }
}
