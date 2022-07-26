package com.imback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imback.dto.DishDto;
import com.imback.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    void saveWithFlavor(DishDto dto);

    DishDto getByIdWithFlavor(Long id);

    void updatWithFlavor(DishDto dto);

    void updateByIds(Integer status, List<Long> ids);

    void deleteByIds(List<Long> ids);
}
