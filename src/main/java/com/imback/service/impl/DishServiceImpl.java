package com.imback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imback.common.CustomException;
import com.imback.dao.DishMapper;
import com.imback.dto.DishDto;
import com.imback.entity.Dish;
import com.imback.entity.DishFlavor;
import com.imback.service.DishFlavorService;
import com.imback.service.DishService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional //开启事务
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品及口味
     * @param dto
     */
    @Override
    public void saveWithFlavor(DishDto dto) {
        //1.先保存菜品表 dish
        this.save(dto);
        Long dishId = dto.getId();//菜品Id
        //2.遍历list,把dish_id保存到口味表
        List<DishFlavor> flavors = dto.getFlavors();
        flavors = flavors.stream().map( (item) -> {
            item.setDishId(dishId);
            return item;
        } ).collect(Collectors.toList());
        /*for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
            dishFlavorService.save(flavor);
        }*/
        //保存菜品口味
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 通过Id查菜品及口味信息
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();
        //查询菜品
        Dish dish = this.getById(id);
        //把查询到的dish拷贝到Dto中
        BeanUtils.copyProperties(dish, dishDto);
        //查询菜品对应的口味 有多条
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(dishFlavorList);

        return dishDto;
    }

    /**
     * 修改菜品及口味
     * @param dto
     */
    @Override
    public void updatWithFlavor(DishDto dto) {
        //1.先修改菜品表 dish
        this.updateById(dto);
        //2.先清理口味，再保存口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId , dto.getId());
        dishFlavorService.remove(queryWrapper);

        //3.保存口味
        List<DishFlavor> flavors = dto.getFlavors();
        //遍历flavors把dishId保存到dish_flavor表中
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 修改启售停售状态
     * @param status
     * @param ids
     */
    @Override
    public void updateByIds(Integer status, List<Long> ids) {

        //通过ids批量查
        List<Dish> dishes = this.listByIds(ids);
        dishes = dishes.stream().map((item) -> {
            item.setStatus(status);
            return item;
        }).collect(Collectors.toList());
        this.updateBatchById(dishes);
    }

    /**
     *  批量删除by ids
     * @param ids
     */
    @Override
    public void deleteByIds(List<Long> ids) {

        //先删dish表,查询状态status=1在售不能删
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.in(Dish::getId , ids);
        dishQueryWrapper.eq(Dish::getStatus , 1); //在售

        int count = this.count(dishQueryWrapper);
        if(count > 0){
            throw new CustomException("菜品还在售卖中，不能删除");
        }

        this.removeByIds(ids);//停售的可以删
        //再删dish_flavor
        LambdaQueryWrapper<DishFlavor> dishFlavorQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorQueryWrapper.in(DishFlavor::getDishId , ids);

        dishFlavorService.remove(dishFlavorQueryWrapper);
    }
}
