package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.service.AiRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiRecommendService aiRecommendService;

    @PostMapping("/recommend")
    public Result<Map<String, Object>> recommend(@RequestBody Map<String, String> params) {
        String description = params.get("description");
        if (description == null || description.trim().isEmpty()) {
            return Result.error("请输入商品描述");
        }
        try {
            List<Product> products = aiRecommendService.recommendByDescription(description.trim());
            Map<String, Object> data = new HashMap<>();
            data.put("products", products);
            data.put("total", products.size());
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("AI推荐服务暂时不可用，请稍后再试");
        }
    }

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> params) {
        String message = (String) params.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error("请输入消息");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) params.get("history");
        if (history == null) {
            history = new ArrayList<>();
        }

        try {
            Map<String, Object> parsed = aiRecommendService.parseIntent(message.trim());
            String reply = parsed.get("reply") != null ? parsed.get("reply").toString() : "让我帮你找找看~";

            List<Product> products = aiRecommendService.recommendByDescription(message.trim());

            if (products.isEmpty()) {
                reply = reply + "\n暂时没有找到完全匹配的商品，为你推荐一些热门好物：";
            }

            Map<String, Object> data = new HashMap<>();
            data.put("reply", reply);
            data.put("products", products);
            data.put("total", products.size());
            return Result.success(data);
        } catch (Exception e) {
            List<Product> products = aiRecommendService.recommendByDescription(message.trim());
            Map<String, Object> data = new HashMap<>();
            data.put("reply", "好的，让我帮你找找\"" + message.trim() + "\"相关的商品~");
            data.put("products", products);
            data.put("total", products.size());
            return Result.success(data);
        }
    }

    @PostMapping("/dialogue")
    public Result<Map<String, Object>> dialogue(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) params.get("history");
        if (history == null || history.isEmpty()) {
            return Result.error("请输入消息");
        }
        try {
            String reply = aiRecommendService.chat(history);
            if (reply == null || reply.isEmpty()) {
                reply = "抱歉，我没有理解你的意思，可以换个方式描述一下吗？";
            }
            Map<String, Object> data = new HashMap<>();
            data.put("reply", reply);
            return Result.success(data);
        } catch (Exception e) {
            Map<String, Object> data = new HashMap<>();
            data.put("reply", "网络有点不稳定，请稍后再试~");
            return Result.success(data);
        }
    }
}
