package com.campus.secondhand.service.impl;

import com.campus.secondhand.config.WechatProperties;
import com.campus.secondhand.dto.WechatLoginDTO;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WechatAuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private WechatProperties wechatProperties;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WechatAuthServiceImpl wechatAuthService;

    @Test
    void login_shouldCreateUserAndReturnTokenForNewWechatUser() {
        WechatLoginDTO dto = new WechatLoginDTO();
        dto.setCode("mini-program-code");

        when(wechatProperties.getAppId()).thenReturn("wx-app-id");
        when(wechatProperties.getAppSecret()).thenReturn("wx-app-secret");
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(any(String.class), eq(String.class)))
                .thenReturn("{\"openid\":\"openid-123\",\"unionid\":\"unionid-456\"}");
        when(userMapper.selectOne(any())).thenReturn(null);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(99L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        Map<String, Object> result = wechatAuthService.login(dto);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForObject(urlCaptor.capture(), eq(String.class));
        String requestUrl = urlCaptor.getValue();
        assertTrue(requestUrl.startsWith("https://api.weixin.qq.com/sns/jscode2session"));
        assertTrue(requestUrl.contains("appid=wx-app-id"));
        assertTrue(requestUrl.contains("secret=wx-app-secret"));
        assertTrue(requestUrl.contains("js_code=mini-program-code"));
        assertTrue(requestUrl.contains("grant_type=authorization_code"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("openid-123", savedUser.getOpenid());
        assertEquals("unionid-456", savedUser.getUnionid());
        assertEquals("user", savedUser.getRole());
        assertEquals(1, savedUser.getStatus());
        assertEquals("UNVERIFIED", savedUser.getVerifyStatus());
        assertNotNull(savedUser.getNickname());
        assertTrue(savedUser.getNickname().startsWith("wx_user_"));
        assertEquals("https://api.dicebear.com/7.x/avataaars/svg?seed=openid-123", savedUser.getAvatar());
        assertNotNull(savedUser.getCreateTime());
        assertNotNull(savedUser.getLastLoginTime());

        ArgumentCaptor<String> tokenPayloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq("token:" + result.get("token")), tokenPayloadCaptor.capture(), eq(72L), eq(TimeUnit.HOURS));
        assertEquals("99:user", tokenPayloadCaptor.getValue());
        assertEquals(99L, result.get("userId"));
        assertEquals(savedUser.getNickname(), result.get("nickname"));
        assertEquals(savedUser.getAvatar(), result.get("avatar"));
        assertEquals("user", result.get("role"));
        assertEquals("UNVERIFIED", result.get("verifyStatus"));
    }

    @Test
    void login_shouldReuseExistingUserForKnownOpenid() {
        WechatLoginDTO dto = new WechatLoginDTO();
        dto.setCode("known-code");

        User existingUser = new User();
        existingUser.setId(7L);
        existingUser.setOpenid("openid-known");
        existingUser.setNickname("existing_user");
        existingUser.setAvatar("https://avatar.example/existing.png");
        existingUser.setRole("user");
        existingUser.setStatus(1);
        existingUser.setVerifyStatus("VERIFIED");
        existingUser.setLastLoginTime(LocalDateTime.of(2024, 1, 1, 10, 0));

        when(wechatProperties.getAppId()).thenReturn("wx-app-id");
        when(wechatProperties.getAppSecret()).thenReturn("wx-app-secret");
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(any(String.class), eq(String.class)))
                .thenReturn("{\"openid\":\"openid-known\"}");
        when(userMapper.selectOne(any())).thenReturn(existingUser);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        Map<String, Object> result = wechatAuthService.login(dto);

        verify(userMapper, never()).insert(any(User.class));
        verify(valueOperations).set(eq("token:" + result.get("token")), eq("7:user"), eq(72L), eq(TimeUnit.HOURS));
        assertEquals(7L, result.get("userId"));
        assertEquals("existing_user", result.get("nickname"));
        assertEquals("https://avatar.example/existing.png", result.get("avatar"));
        assertEquals("user", result.get("role"));
        assertEquals("VERIFIED", result.get("verifyStatus"));
    }

    @Test
    void login_shouldRejectMissingCode() {
        WechatLoginDTO dto = new WechatLoginDTO();

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> wechatAuthService.login(dto));

        assertEquals("微信登录缺少临时凭证", exception.getMessage());
    }

    @Test
    void login_shouldRejectDisabledExistingUser() {
        WechatLoginDTO dto = new WechatLoginDTO();
        dto.setCode("disabled-code");

        User existingUser = new User();
        existingUser.setId(8L);
        existingUser.setOpenid("openid-disabled");
        existingUser.setNickname("disabled_user");
        existingUser.setAvatar("https://avatar.example/disabled.png");
        existingUser.setRole("user");
        existingUser.setStatus(0);

        when(wechatProperties.getAppId()).thenReturn("wx-app-id");
        when(wechatProperties.getAppSecret()).thenReturn("wx-app-secret");
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(any(String.class), eq(String.class)))
                .thenReturn("{\"openid\":\"openid-disabled\"}");
        when(userMapper.selectOne(any())).thenReturn(existingUser);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> wechatAuthService.login(dto));

        assertEquals("账号已被禁用", exception.getMessage());
    }

    @Test
    void login_shouldWrapWechatTransportFailure() {
        WechatLoginDTO dto = new WechatLoginDTO();
        dto.setCode("error-code");

        when(wechatProperties.getAppId()).thenReturn("wx-app-id");
        when(wechatProperties.getAppSecret()).thenReturn("wx-app-secret");
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(any(String.class), eq(String.class)))
                .thenThrow(new RestClientException("timeout"));

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> wechatAuthService.login(dto));

        assertEquals("微信登录失败，请稍后重试", exception.getMessage());
    }

    @Test
    void login_shouldPropagateWechatErrorMessageFromJsonResponse() {
        WechatLoginDTO dto = new WechatLoginDTO();
        dto.setCode("invalid-code");

        when(wechatProperties.getAppId()).thenReturn("wx-app-id");
        when(wechatProperties.getAppSecret()).thenReturn("wx-app-secret");
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(any(String.class), eq(String.class)))
                .thenReturn("{\"errcode\":40029,\"errmsg\":\"invalid code\"}");

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> wechatAuthService.login(dto));

        assertEquals("invalid code", exception.getMessage());
        verify(userMapper, never()).selectOne(any());
    }
}
