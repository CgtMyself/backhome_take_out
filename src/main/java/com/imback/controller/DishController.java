package com.imback.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imback.common.Result;
import com.imback.dto.DishDto;
import com.imback.entity.Category;
import com.imback.entity.Dish;
import com.imback.entity.DishFlavor;
import com.imback.service.CategoryService;
import com.imback.service.DishFlavorService;
import com.imback.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品分类
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDto dto){
        log.info("dto = {}",dto);

        dishService.saveWithFlavor(dto);
        //新增菜品需要 清理某个分类下的缓存保证缓存中数据和数据库中的一致
        String key = "dish_"+dto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return Result.success("新增成功");
    }

    /**
     * 菜品条件分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page<DishDto>> page(Integer page, Integer pageSize, String name){
        //分页构造器
        Page<Dish> pageInfo = new Page(page,pageSize);
        Page<DishDto> dtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Dish::getIsDeleted,0);
        //添加过滤条件
        queryWrapper.like(!StringUtils.isEmpty(name), Dish::getName, name);
        //排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo, queryWrapper);
        //对象拷贝(拷贝的是和分页有关的属性)
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");//records不拷贝
        //获取菜品列表
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> dtoList = records.stream().map( (item) -> {
            DishDto dishDto = new DishDto();
            //拷贝的是和Dto有关的数据，后边追加CategoryName到Dto
            BeanUtils.copyProperties(item , dishDto);

            Category category = categoryService.getById(item.getCategoryId());
            //把name仍到dto中
            dishDto.setCategoryName(category.getName());

            return dishDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dtoList);

        return Result.success(dtoPage);
    }

    /**
     * 根据Id查菜品及对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDto> getById(@PathVariable Long id){
        log.info("id = {}",id);

        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dto
     * @return
     */
    @PutMapping
    public Result<String> edit(@RequestBody DishDto dto){
        log.info("dto = {}",dto);

        dishService.updatWithFlavor(dto);
        //修改菜品需要 清理某个分类下的缓存
        String key = "dish_"+dto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return Result.success("修改成功");
    }

    /**
     * 停售启售及批量停售启售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> updateStatus(@PathVariable Integer status,@RequestParam List<Long> ids){
        log.info("status = {},ids = {}",status,ids);
        dishService.updateByIds(status,ids);

        return Result.success("修改成功");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> deleteByIds(@RequestParam List<Long> ids){
        log.info("ids = {}",ids);

        dishService.deleteByIds(ids);
        return Result.success("删除成功");
    }

    /**
     * 查询菜品列表（及口味数据，移动端需要口味数据）
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public Result<List<DishDto>> selectDishAndFlavor(Dish dish){
        log.info("dish = {}",dish);
        List<DishDto> dtoList = null;
                //菜品列表加入redis缓存
        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //先从redis中获取数据
        dtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //判断
        if(dtoList!=null){
            return Result.success(dtoList);
        }

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null , Dish::getCategoryId ,dish.getCategoryId());
        //添加条件，查询状态为1的菜品（启售状态1  停售状态0）
        queryWrapper.eq(Dish::getStatus , 1);
        //排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //执行
        List<Dish> dishList = dishService.list(queryWrapper);
        //遍历dishList
        dtoList = dishList.stream().map(( item ) -> {
            DishDto dishDto = new DishDto();
            //把dish中的数据拷贝到dishDto
            BeanUtils.copyProperties(item , dishDto);
            //把name仍到dto中
            dishDto.setCategoryName(item.getName());
            //然后通过dish_id查询对应的口味表信息
            LambdaQueryWrapper<DishFlavor> dishFlavorQueryWrapper = new LambdaQueryWrapper();
            dishFlavorQueryWrapper.eq(DishFlavor::getDishId , item.getId());
            List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorQueryWrapper);
            //把dish_id对应的口味表中的数据封装到dishDto
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //缓存中没有菜品把查到的菜品缓存到redis，设置过期时间为一小时
        redisTemplate.opsForValue().set(key,dtoList,60, TimeUnit.MINUTES);

        return Result.success(dtoList);
    }

}
