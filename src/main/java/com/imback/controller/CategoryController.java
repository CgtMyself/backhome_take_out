package com.imback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imback.common.Result;
import com.imback.entity.Category;
import com.imback.entity.Dish;
import com.imback.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param category
     * @return
     */
    @PostMapping
    public Result<String> addCate(@RequestBody Category category){
        categoryService.save(category);
        return Result.success("新增菜品成功");
    }

    /**
     * 查询菜品
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize){
        //分页构造器
        Page pageInfo = new Page(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //排序
        queryWrapper.orderByAsc(Category::getSort);

        categoryService.page(pageInfo,queryWrapper);
        return Result.success(pageInfo);
    }

    /**
     * 通过Id修改菜品及套餐
     * @param category
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return Result.success("修改成功");
    }

    /**
     * 通过Id删除菜品及套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delectByIds(Long ids){
        log.info("ids = {}",ids);
        categoryService.remove(ids);
        return Result.success("修改成功");
    }

    /**
     * 查询菜品分类列表（type=1）及套餐分类列表（type=2）
     * @param category
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> selectByType(Category category){
        log.info("category = {}",category);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null , Category::getType ,category.getType());
        //排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //执行
        List<Category> categoryList = categoryService.list(queryWrapper);
        return Result.success(categoryList);
    }

}
