package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.entity.Category;
import com.campus.secondhand.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> listEnabledCategories() {
        return Result.success(categoryService.listEnabledCategories());
    }

    @GetMapping("/admin/list")
    public Result<List<Category>> listAllCategories(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限操作");
        }
        return Result.success(categoryService.listAllCategories());
    }

    @PostMapping("/save")
    public Result<String> saveCategory(@RequestBody Category category, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限操作");
        }
        categoryService.saveCategory(category);
        return Result.success("已保存");
    }

    @PostMapping("/updateStatus")
    public Result<String> updateStatus(@RequestParam Integer id,
                                       @RequestParam Integer status,
                                       HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error("无权限操作");
        }
        categoryService.updateStatus(id, status);
        return Result.success("已更新");
    }

    private boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(request.getAttribute("currentUserRole"));
    }
}
