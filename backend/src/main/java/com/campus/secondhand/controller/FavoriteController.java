package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/add")
    public Result<String> addFavorite(@RequestParam Long productId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        favoriteService.favorite(currentUserId, productId);
        return Result.success("收藏成功");
    }

    @PostMapping("/remove")
    public Result<String> removeFavorite(@RequestParam Long productId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        favoriteService.unfavorite(currentUserId, productId);
        return Result.success("已取消收藏");
    }

    @GetMapping("/list")
    public Result<List<Product>> listFavorites(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        List<Product> products = favoriteService.listByUserId(currentUserId);
        return Result.success(products);
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> favoriteStatus(@RequestParam Long productId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favoriteService.isFavorited(currentUserId, productId));
        return Result.success(result);
    }
}
