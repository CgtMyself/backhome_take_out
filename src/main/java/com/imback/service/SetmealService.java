package com.imback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imback.dto.SetmealDto;
import com.imback.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);

    SetmealDto getByIdWithDish(Long id);

    void updateWithDish(SetmealDto setmealDto);

    void updateSetmealstatus(Integer status, List<Long> ids);

    void deleteWithSetmeal(List<Long> ids);
}
