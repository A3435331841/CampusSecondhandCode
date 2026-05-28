package com.campus.secondhand.service.impl;

import com.campus.secondhand.config.TencentMapProperties;
import com.campus.secondhand.dto.LocationSearchDTO;
import com.campus.secondhand.service.TencentMapService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TencentMapServiceImpl implements TencentMapService {

    private static final List<Map<String, Object>> CAMPUS_LOCATIONS = List.of(
            Map.of("placeName", "图书馆北门", "address", "校园图书馆北门广场", "lat", 22.533300, "lng", 113.930400),
            Map.of("placeName", "第一食堂", "address", "学生生活区第一食堂门口", "lat", 22.534100, "lng", 113.931200),
            Map.of("placeName", "学生宿舍区", "address", "学生宿舍区主入口", "lat", 22.532600, "lng", 113.929500),
            Map.of("placeName", "快递服务站", "address", "校园快递服务站取件区", "lat", 22.531900, "lng", 113.928700),
            Map.of("placeName", "教学楼A座", "address", "教学楼A座大厅", "lat", 22.533900, "lng", 113.932100),
            Map.of("placeName", "体育馆", "address", "校园体育馆正门", "lat", 22.535000, "lng", 113.929900),
            Map.of("placeName", "学生活动中心", "address", "学生活动中心一楼", "lat", 22.532200, "lng", 113.931700),
            Map.of("placeName", "校门口", "address", "学校正门集合点", "lat", 22.530900, "lng", 113.930000)
    );

    private final TencentMapProperties tencentMapProperties;
    private final RestTemplateBuilder restTemplateBuilder;

    public TencentMapServiceImpl(TencentMapProperties tencentMapProperties,
                                 RestTemplateBuilder restTemplateBuilder) {
        this.tencentMapProperties = tencentMapProperties;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Override
    public Map<String, Object> reverseGeocode(BigDecimal lat, BigDecimal lng) {
        ensureConfigured();
        String url = UriComponentsBuilder.fromHttpUrl("https://apis.map.qq.com/ws/geocoder/v1/")
                .queryParam("location", lat + "," + lng)
                .queryParam("key", tencentMapProperties.getKey())
                .build()
                .toUriString();

        Map<String, Object> response = fetch(url);
        Map<String, Object> result = asMap(response.get("result"));
        Map<String, Object> formatted = asMap(result.get("formatted_addresses"));

        Map<String, Object> location = new HashMap<>();
        location.put("placeName", stringValue(formatted.get("recommend")));
        location.put("address", stringValue(result.get("address")));
        location.put("lat", lat);
        location.put("lng", lng);
        return location;
    }

    @Override
    public List<Map<String, Object>> searchSuggestions(LocationSearchDTO dto) {
        ensureConfigured();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://apis.map.qq.com/ws/place/v1/suggestion")
                .queryParam("keyword", dto.getKeyword())
                .queryParam("key", tencentMapProperties.getKey());
        if (dto.getLat() != null && dto.getLng() != null) {
            builder.queryParam("location", dto.getLat() + "," + dto.getLng());
        }
        String url = builder.build().toUriString();

        Map<String, Object> response = fetch(url);
        List<Map<String, Object>> suggestions = new ArrayList<>();
        Object data = response.get("data");
        if (data instanceof List<?> items) {
            for (Object item : items) {
                Map<String, Object> map = asMap(item);
                Map<String, Object> loc = asMap(map.get("location"));
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("placeName", stringValue(map.get("title")));
                suggestion.put("address", stringValue(map.get("address")));
                suggestion.put("lat", loc.get("lat"));
                suggestion.put("lng", loc.get("lng"));
                suggestions.add(suggestion);
            }
        }
        return suggestions.isEmpty() ? fallbackSuggestions(dto) : suggestions;
    }

    private List<Map<String, Object>> fallbackSuggestions(LocationSearchDTO dto) {
        String keyword = dto.getKeyword() == null ? "" : dto.getKeyword().trim();
        List<Map<String, Object>> matched = new ArrayList<>();
        for (Map<String, Object> location : CAMPUS_LOCATIONS) {
            String placeName = stringValue(location.get("placeName"));
            String address = stringValue(location.get("address"));
            if (keyword.isBlank() || placeName.contains(keyword) || address.contains(keyword)) {
                matched.add(new HashMap<>(location));
            }
        }
        if (!matched.isEmpty()) {
            return matched;
        }
        List<Map<String, Object>> all = new ArrayList<>();
        for (Map<String, Object> location : CAMPUS_LOCATIONS) {
            all.add(new HashMap<>(location));
        }
        return all;
    }

    private void ensureConfigured() {
        if (tencentMapProperties.getKey() == null || tencentMapProperties.getKey().isBlank()) {
            throw new IllegalStateException("腾讯地图 Key 未配置");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetch(String url) {
        try {
            Map<String, Object> response = restTemplateBuilder.build().getForObject(url, Map.class);
            return response == null ? Map.of() : response;
        } catch (RestClientException exception) {
            throw new IllegalStateException("腾讯地图请求失败", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
