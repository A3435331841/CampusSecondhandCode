package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.secondhand.entity.Category;
import com.campus.secondhand.mapper.CategoryMapper;
import com.campus.secondhand.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> listEnabledCategories() {
        List<Category> categories = new ArrayList<>(categoryMapper.selectList(new QueryWrapper<Category>().eq("status", 1)));
        categories.sort(Comparator.comparing(Category::getSortNo, Comparator.nullsLast(Integer::compareTo)));
        return categories;
    }

    @Override
    public List<Category> listAllCategories() {
        List<Category> categories = new ArrayList<>(categoryMapper.selectList(new QueryWrapper<>()));
        categories.sort(Comparator.comparing(Category::getSortNo, Comparator.nullsLast(Integer::compareTo)));
        return categories;
    }

    @Override
    public void saveCategory(Category category) {
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getCreateTime() == null) {
            category.setCreateTime(LocalDateTime.now());
        }
        if (category.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        categoryMapper.updateById(category);
    }
}
