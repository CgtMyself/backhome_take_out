package com.imback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imback.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
