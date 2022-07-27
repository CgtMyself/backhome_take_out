package com.imback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imback.common.Result;
import com.imback.dto.DishDto;
import com.imback.dto.SetmealDishDto;
import com.imback.dto.SetmealDto;
import com.imback.entity.Category;
import com.imback.entity.Dish;
import com.imback.entity.Setmeal;
import com.imback.entity.SetmealDish;
import com.imback.service.CategoryService;
import com.imback.service.DishService;
import com.imback.service.SetmealDishService;
import com.imback.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private DishService dishService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache" , allEntries = false)
    public Result<String> save(@RequestBody SetmealDto setmealDto){
        log.info("setmealDto = {}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return Result.success("新增成功");
    }

    /**
     * 套餐分页条件查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page<SetmealDto>> page(Integer page,Integer pageSize,String name){

        //分页构造器
        Page<Setmeal> setmealPage = new Page<>(page ,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.like(!StringUtils.isEmpty(name), Setmeal::getName, name);
        setmealQueryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(setmealPage, setmealQueryWrapper);

        //拷贝
        //先拷贝分页相关的数据 page，pageSize等
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");
        //获取套餐列表
        List<Setmeal> records = setmealPage.getRecords();
        //遍历，把CategoryName加上
        List<SetmealDto> setmealList = records.stream().map((item) -> {
            SetmealDto dto = new SetmealDto();
            BeanUtils.copyProperties(item, dto);
            Category category = categoryService.getById(item.getCategoryId());
            dto.setCategoryName(category.getName());
            return dto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealList);

        return Result.success(setmealDtoPage);
    }

    /**
     * 根据Id查菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealDto> getById(@PathVariable Long id){
        log.info("id = {}",id);

        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return Result.success(setmealDto);
    }

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmealCache" , allEntries = false)
    public Result<String> edit(@RequestBody SetmealDto setmealDto){

        setmealService.updateWithDish(setmealDto);
        return Result.success("修改成功");
    }

    /**
     * 启售停售及批量启售停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> updateStatus(@PathVariable Integer status, @RequestParam List<Long> ids){
        setmealService.updateSetmealstatus(status,ids);
        return Result.success("修改成功");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache" , allEntries = true) //删除spring cache缓存，删除所有key为setmealCache的套餐数据
    public Result<String> deleteByIds(@RequestParam List<Long> ids){
        setmealService.deleteWithSetmeal(ids);
        return Result.success("删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public Result<List<SetmealDto>> selectSetmealAndDish(Setmeal setmeal){
        log.info("setmeal = {}",setmeal);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null , Setmeal::getCategoryId ,setmeal.getCategoryId());
        //添加条件，查询状态为1的菜品（启售状态1  停售状态0）
        queryWrapper.eq(Setmeal::getStatus , 1);
        //排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        List<SetmealDto> setmealDtoList = setmealList.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item , setmealDto);
            //通过套餐Id查对应的菜品
            LambdaQueryWrapper<SetmealDish> setmealDishQueryWrapper = new LambdaQueryWrapper<>();
            setmealDishQueryWrapper.eq(SetmealDish::getSetmealId,item.getId());
            List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishQueryWrapper);

            setmealDto.setSetmealDishes(setmealDishList);
            return setmealDto;
        }).collect(Collectors.toList());

        return Result.success(setmealDtoList);
    }

    /**
     * 手机端套餐详情
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public Result<List<SetmealDishDto>> setmealDetail(@PathVariable Long id){
        log.info("id = {}",id);
        //查套餐明细
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        queryWrapper.orderByAsc(SetmealDish::getSort);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        List<SetmealDishDto> setmealDishDtoList = setmealDishList.stream().map((item) -> {
            SetmealDishDto setmealDishDto = new SetmealDishDto();
            //拷贝
            BeanUtils.copyProperties(item,setmealDishDto);
            //查询dish表获取对应的 image和description
            Dish dish = dishService.getById(item.getDishId());
            setmealDishDto.setImage(dish.getImage());
            setmealDishDto.setDescription(dish.getDescription());
            return setmealDishDto;
        }).collect(Collectors.toList());

        return Result.success(setmealDishDtoList);
    }
}
