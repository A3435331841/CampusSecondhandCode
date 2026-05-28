package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.secondhand.config.DeepSeekProperties;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.service.AiRecommendService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AiRecommendServiceImpl implements AiRecommendService {

    private static final Logger log = LoggerFactory.getLogger(AiRecommendServiceImpl.class);

    @Autowired
    private DeepSeekProperties properties;

    @Autowired
    private ProductMapper productMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    private static final String SYSTEM_PROMPT =
            "你是校园二手交易平台的AI助手。根据用户的描述，提取搜索关键词并生成友好的回复。\n" +
            "要求：\n" +
            "1. keywords: 提取多个短关键词（每个2-3个字），用于模糊搜索商品标题。例如\"考研资料\"应拆分为[\"考研\",\"资料\",\"备考\"]，\"笔记本电脑\"应拆分为[\"笔记本\",\"电脑\",\"laptop\"]\n" +
            "2. category: 判断所属分类(数码电子/图书教材/服饰鞋包/交通出行/生活用品/其他闲置)，不确定返回null\n" +
            "3. maxPrice: 如果用户提到价格预算就给出数字，否则null\n" +
            "4. reply: 用一句话友好地回复用户，告诉他你正在帮他找什么\n" +
            "返回JSON格式：{\"keywords\":[...], \"category\":\"...\", \"maxPrice\":null, \"reply\":\"...\"}";

    private static final Map<String, Integer> CATEGORY_MAP = new HashMap<>();
    static {
        CATEGORY_MAP.put("数码电子", 1);
        CATEGORY_MAP.put("图书教材", 2);
        CATEGORY_MAP.put("服饰鞋包", 3);
        CATEGORY_MAP.put("交通出行", 4);
        CATEGORY_MAP.put("生活用品", 5);
        CATEGORY_MAP.put("其他闲置", 6);
    }

    @Override
    public List<Product> recommendByDescription(String description) {
        try {
            Map<String, Object> parsed = callDeepSeekApi(description);
            List<Product> results = searchProducts(parsed);
            if (results.isEmpty()) {
                results = fallbackSearch(description);
            }
            if (results.isEmpty()) {
                results = getPopularProducts();
            }
            return results;
        } catch (Exception e) {
            log.error("AI推荐失败，回退到关键词搜索: {}", e.getMessage(), e);
            List<Product> results = fallbackSearch(description);
            if (results.isEmpty()) {
                results = getPopularProducts();
            }
            return results;
        }
    }

    public Map<String, Object> parseIntent(String description) {
        try {
            return callDeepSeekApi(description);
        } catch (Exception e) {
            log.error("DeepSeek意图解析失败: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("keywords", List.of(description));
            fallback.put("reply", "好的，让我帮你找找\"" + description + "\"相关的商品~");
            return fallback;
        }
    }

    public String chat(List<Map<String, String>> history) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", properties.getModel());
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 200);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是校园二手交易平台的AI助手，名叫\"小二\"。你帮助用户寻找需要的二手商品。" +
                    "回复要简短友好（不超过50字），可以追问用户的具体需求，比如预算、品牌偏好、新旧程度等。" +
                    "如果用户的需求不明确，主动引导他描述想要什么。不要使用markdown格式。");
            messages.add(systemMsg);
            messages.addAll(history);

            requestBody.put("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            Request request = new Request.Builder()
                    .url(properties.getBaseUrl() + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + properties.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) return null;
                String body = response.body().string();
                JsonNode root = objectMapper.readTree(body);
                return root.path("choices").path(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.error("AI对话失败: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> callDeepSeekApi(String description) throws Exception {
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("model", properties.getModel());
        requestBodyMap.put("temperature", 0.1);

        Map<String, String> responseFormat = new HashMap<>();
        responseFormat.put("type", "json_object");
        requestBodyMap.put("response_format", responseFormat);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", description);
        messages.add(userMsg);

        requestBodyMap.put("messages", messages);

        String jsonBody = objectMapper.writeValueAsString(requestBodyMap);

        Request request = new Request.Builder()
                .url(properties.getBaseUrl() + "/chat/completions")
                .addHeader("Authorization", "Bearer " + properties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("DeepSeek API调用失败, HTTP状态码: " + response.code());
            }

            String responseBody = response.body().string();
            log.info("DeepSeek API响应: {}", responseBody);

            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").path(0).path("message").path("content").asText();

            return objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        }
    }

    private List<Product> searchProducts(Map<String, Object> parsed) {
        List<String> keywords = new ArrayList<>();
        Object keywordsObj = parsed.get("keywords");
        if (keywordsObj instanceof List) {
            for (Object kw : (List<?>) keywordsObj) {
                if (kw != null) keywords.add(kw.toString());
            }
        }

        String category = parsed.get("category") != null ? parsed.get("category").toString() : null;
        if ("null".equals(category)) category = null;

        BigDecimal maxPrice = null;
        Object maxPriceObj = parsed.get("maxPrice");
        if (maxPriceObj != null && !"null".equals(maxPriceObj.toString())) {
            try {
                maxPrice = new BigDecimal(maxPriceObj.toString());
            } catch (NumberFormatException ignored) {}
        }

        Set<Long> seenIds = new HashSet<>();
        List<Product> results = new ArrayList<>();
        Integer categoryId = (category != null) ? CATEGORY_MAP.get(category) : null;

        for (String kw : keywords) {
            if (kw.length() < 2) continue;
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1);
            wrapper.and(w -> w.like("title", kw).or().like("description", kw));
            if (categoryId != null) wrapper.eq("category_id", categoryId);
            if (maxPrice != null) wrapper.le("price", maxPrice);
            wrapper.orderByDesc("create_time");
            wrapper.last("LIMIT 20");

            List<Product> found = productMapper.selectList(wrapper);
            for (Product p : found) {
                if (seenIds.add(p.getId())) results.add(p);
            }
        }

        if (results.size() < 5 && !keywords.isEmpty()) {
            for (String kw : keywords) {
                if (kw.length() < 2) continue;
                QueryWrapper<Product> wrapper = new QueryWrapper<>();
                wrapper.eq("status", 1);
                wrapper.and(w -> w.like("title", kw).or().like("description", kw));
                if (maxPrice != null) wrapper.le("price", maxPrice);
                wrapper.orderByDesc("create_time");
                wrapper.last("LIMIT 10");

                List<Product> found = productMapper.selectList(wrapper);
                for (Product p : found) {
                    if (seenIds.add(p.getId())) results.add(p);
                }
            }
        }

        return results.stream().limit(20).collect(Collectors.toList());
    }

    private List<Product> fallbackSearch(String description) {
        Set<Long> seenIds = new HashSet<>();
        List<Product> results = new ArrayList<>();

        Set<String> segments = new LinkedHashSet<>();
        String[] parts = description.split("[\\s，。、！？,.!?]+");
        for (String part : parts) {
            if (part.length() >= 2) {
                segments.add(part);
                if (part.length() > 2) {
                    for (int i = 0; i <= part.length() - 2; i++) {
                        segments.add(part.substring(i, i + 2));
                    }
                }
            }
        }

        for (String seg : segments) {
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1);
            wrapper.and(w -> w.like("title", seg).or().like("description", seg));
            wrapper.orderByDesc("create_time");
            wrapper.last("LIMIT 10");

            List<Product> found = productMapper.selectList(wrapper);
            for (Product p : found) {
                if (seenIds.add(p.getId())) results.add(p);
            }
            if (results.size() >= 20) break;
        }

        return results.stream().limit(20).collect(Collectors.toList());
    }

    private List<Product> getPopularProducts() {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("view_count");
        wrapper.last("LIMIT 10");
        return productMapper.selectList(wrapper);
    }
}
