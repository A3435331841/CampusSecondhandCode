package com.campus.secondhand.service.impl;

import com.campus.secondhand.entity.Category;
import com.campus.secondhand.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void listEnabledCategories_shouldReturnSortedActiveCategories() {
        Category categoryA = new Category();
        categoryA.setId(2);
        categoryA.setName("Books");
        categoryA.setSortNo(2);
        categoryA.setStatus(1);

        Category categoryB = new Category();
        categoryB.setId(1);
        categoryB.setName("Digital");
        categoryB.setSortNo(1);
        categoryB.setStatus(1);

        when(categoryMapper.selectList(any())).thenReturn(List.of(categoryA, categoryB));

        List<Category> categories = categoryService.listEnabledCategories();

        assertEquals(2, categories.size());
        assertEquals(1, categories.get(0).getSortNo());
        assertEquals("Digital", categories.get(0).getName());
    }

    @Test
    void saveCategory_shouldFillDefaults() {
        Category category = new Category();
        category.setName("Accessories");

        categoryService.saveCategory(category);

        assertEquals(1, category.getStatus());
        assertNotNull(category.getCreateTime());
        verify(categoryMapper).insert(category);
    }
}
