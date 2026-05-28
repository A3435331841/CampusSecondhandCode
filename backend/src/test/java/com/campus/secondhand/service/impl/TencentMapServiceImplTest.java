package com.campus.secondhand.service.impl;

import com.campus.secondhand.config.TencentMapProperties;
import com.campus.secondhand.dto.LocationSearchDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TencentMapServiceImplTest {

    @Mock
    private TencentMapProperties tencentMapProperties;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TencentMapServiceImpl tencentMapService;

    @Test
    void reverseGeocode_shouldReturnPlaceNameAndAddress() {
        when(tencentMapProperties.getKey()).thenReturn("map-key");
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(any(String.class), eq(Map.class))).thenReturn(Map.of(
                "status", 0,
                "result", Map.of(
                        "address", "Shenzhen Nanshan District",
                        "formatted_addresses", Map.of("recommend", "Library Gate")
                )
        ));

        Map<String, Object> location = tencentMapService.reverseGeocode(new BigDecimal("22.500000"), new BigDecimal("113.900000"));

        verify(restTemplate).getForObject(any(String.class), eq(Map.class));
        assertEquals("Library Gate", location.get("placeName"));
        assertEquals("Shenzhen Nanshan District", location.get("address"));
    }

    @Test
    void searchSuggestions_shouldReturnLocationOptions() {
        when(tencentMapProperties.getKey()).thenReturn("map-key");
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(any(String.class), eq(Map.class))).thenReturn(Map.of(
                "status", 0,
                "data", List.of(
                        Map.of(
                                "title", "Library Gate",
                                "address", "Library Road",
                                "location", Map.of("lat", 22.5, "lng", 113.9)
                        )
                )
        ));

        LocationSearchDTO dto = new LocationSearchDTO();
        dto.setKeyword("Library");
        dto.setLat(new BigDecimal("22.500000"));
        dto.setLng(new BigDecimal("113.900000"));

        List<Map<String, Object>> suggestions = tencentMapService.searchSuggestions(dto);

        assertFalse(suggestions.isEmpty());
        assertEquals("Library Gate", suggestions.get(0).get("placeName"));
        assertEquals("Library Road", suggestions.get(0).get("address"));
        assertTrue(String.valueOf(suggestions.get(0).get("lat")).startsWith("22.5"));
    }

    @Test
    void searchSuggestions_shouldFallbackToCampusLocationsWhenTencentReturnsNoData() {
        when(tencentMapProperties.getKey()).thenReturn("map-key");
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(any(String.class), eq(Map.class))).thenReturn(Map.of(
                "status", 0,
                "data", List.of()
        ));

        LocationSearchDTO dto = new LocationSearchDTO();
        dto.setKeyword("图书馆");

        List<Map<String, Object>> suggestions = tencentMapService.searchSuggestions(dto);

        assertFalse(suggestions.isEmpty());
        assertEquals("图书馆北门", suggestions.get(0).get("placeName"));
        assertTrue(String.valueOf(suggestions.get(0).get("address")).contains("图书馆"));
    }

    @Test
    void reverseGeocode_shouldRejectWhenKeyMissing() {
        when(tencentMapProperties.getKey()).thenReturn("");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tencentMapService.reverseGeocode(new BigDecimal("22.500000"), new BigDecimal("113.900000"))
        );

        assertEquals("腾讯地图 Key 未配置", exception.getMessage());
    }

    @Test
    void searchSuggestions_shouldWrapTransportFailure() {
        when(tencentMapProperties.getKey()).thenReturn("map-key");
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(any(String.class), eq(Map.class)))
                .thenThrow(new org.springframework.web.client.RestClientException("timeout"));

        LocationSearchDTO dto = new LocationSearchDTO();
        dto.setKeyword("Library");

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> tencentMapService.searchSuggestions(dto));

        assertEquals("腾讯地图请求失败", exception.getMessage());
    }
}
