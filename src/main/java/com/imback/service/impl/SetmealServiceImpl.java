package com.imback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imback.common.CustomException;
import com.imback.dao.SetmealMapper;
import com.imback.dto.SetmealDto;
import com.imback.entity.Setmeal;
import com.imback.entity.SetmealDish;
import com.imback.service.SetmealDishService;
import com.imback.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional //操作多张表需要开启事务
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐
     * @param setmealDto
     */
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //1.先保存套餐
        this.save(setmealDto);
        Long setmealId = setmealDto.getId();
        //2.保存套餐对应的菜品
        //遍历菜品把套餐Id放进去
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 根据套餐Id查菜品
     * @param id
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        SetmealDto setmealDto = new SetmealDto();
        //先查套餐信息
        Setmeal setmeal = this.getById(id);
        //拷贝
        BeanUtils.copyProperties(setmeal,setmealDto);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishlist = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishlist);
        return setmealDto;
    }

    /**
     * 修改套餐
     * @param setmealDto
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);
        //先清理子表
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId ,setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //再保存子表
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 启售停售及批量启售停售
     * @param status
     * @param ids
     */
    @Override
    public void updateSetmealstatus(Integer status, List<Long> ids) {
        List<Setmeal> setmeals = this.listByIds(ids);
        //遍历然后改状态
        setmeals = setmeals.stream().map((item) -> {
            item.setStatus(status);
            return item;
        }).collect(Collectors.toList());
        this.updateBatchById(setmeals);
    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    public void deleteWithSetmeal(List<Long> ids) {
        //先判断setmeal表 状态 在售 停售
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.in(Setmeal::getId , ids);
        setmealQueryWrapper.eq(Setmeal::getStatus , 1);//在售
        int count = this.count(setmealQueryWrapper);
        if(count > 0){
            //在售
            throw new CustomException("套餐还在售卖中，不能删除");
        }
        //停售
        this.removeByIds(ids);
        //删套餐对应的菜品表setmeal_dish
        LambdaQueryWrapper<SetmealDish> setmealDishQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishQueryWrapper.in(SetmealDish::getSetmealId , ids);

        setmealDishService.remove(setmealDishQueryWrapper);
    }
}
