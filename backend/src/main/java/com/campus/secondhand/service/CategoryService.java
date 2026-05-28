package com.campus.secondhand.service;

import com.campus.secondhand.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> listEnabledCategories();

    List<Category> listAllCategories();

    void saveCategory(Category category);

    void updateStatus(Integer id, Integer status);
}
