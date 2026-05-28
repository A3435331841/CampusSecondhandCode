package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.secondhand.config.WechatProperties;
import com.campus.secondhand.dto.WechatLoginDTO;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.WechatAuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class WechatAuthServiceImpl implements WechatAuthService {

    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_EXPIRE_HOURS = 72;
    private static final String DEFAULT_ROLE = "user";
    private static final String DEFAULT_VERIFY_STATUS = "UNVERIFIED";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final WechatProperties wechatProperties;
    private final RestTemplateBuilder restTemplateBuilder;

    public WechatAuthServiceImpl(UserMapper userMapper,
                                 StringRedisTemplate stringRedisTemplate,
                                 WechatProperties wechatProperties,
                                 RestTemplateBuilder restTemplateBuilder) {
        this.userMapper = userMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.wechatProperties = wechatProperties;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Override
    public Map<String, Object> login(WechatLoginDTO loginDTO) {
        if (loginDTO == null || loginDTO.getCode() == null || loginDTO.getCode().isBlank()) {
            throw new IllegalStateException("微信登录缺少临时凭证");
        }

        Map<String, Object> response = fetchCode2Session(loginDTO.getCode());
        String openid = asText(response.get("openid"));
        if (openid == null || openid.isBlank()) {
            String errMsg = asText(response.get("errmsg"));
            throw new IllegalStateException(errMsg == null || errMsg.isBlank() ? "微信登录失败" : errMsg);
        }

        String unionid = asText(response.get("unionid"));
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("openid", openid));
        LocalDateTime now = LocalDateTime.now();
        if (user == null) {
            user = buildNewUser(openid, unionid, now);
            userMapper.insert(user);
        } else {
            updateExistingUser(user, unionid, now);
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalStateException("账号已被禁用");
        }

        String role = normalizeRole(user.getRole());
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(
                TOKEN_PREFIX + token,
                user.getId() + ":" + role,
                TOKEN_EXPIRE_HOURS,
                TimeUnit.HOURS
        );

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("role", role);
        result.put("verifyStatus", user.getVerifyStatus());
        return result;
    }

    private Map<String, Object> fetchCode2Session(String code) {
        if (wechatProperties.getAppId() == null || wechatProperties.getAppId().isBlank()
                || wechatProperties.getAppSecret() == null || wechatProperties.getAppSecret().isBlank()) {
            throw new IllegalStateException("微信登录未配置");
        }

        String url = UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
                .queryParam("appid", wechatProperties.getAppId())
                .queryParam("secret", wechatProperties.getAppSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build()
                .toUriString();

        try {
            String response = restTemplateBuilder.build().getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                return Map.of();
            }
            return OBJECT_MAPPER.readValue(response, MAP_TYPE);
        } catch (RestClientException exception) {
            throw new IllegalStateException("微信登录失败，请稍后重试", exception);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("微信登录返回无效响应", exception);
        }
    }

    private User buildNewUser(String openid, String unionid, LocalDateTime now) {
        User user = new User();
        user.setOpenid(openid);
        user.setUnionid(unionid);
        user.setNickname(buildDefaultNickname(openid));
        user.setAvatar(buildDefaultAvatar(openid));
        user.setRole(DEFAULT_ROLE);
        user.setStatus(1);
        user.setVerifyStatus(DEFAULT_VERIFY_STATUS);
        user.setCreditScore(100);
        user.setFavoriteCount(0);
        user.setCreateTime(now);
        user.setLastLoginTime(now);
        return user;
    }

    private void updateExistingUser(User user, String unionid, LocalDateTime now) {
        boolean changed = false;
        if ((user.getUnionid() == null || user.getUnionid().isBlank()) && unionid != null && !unionid.isBlank()) {
            user.setUnionid(unionid);
            changed = true;
        }
        if (user.getNickname() == null || user.getNickname().isBlank()) {
            user.setNickname(buildDefaultNickname(user.getOpenid()));
            changed = true;
        }
        if (user.getAvatar() == null || user.getAvatar().isBlank()) {
            user.setAvatar(buildDefaultAvatar(user.getOpenid()));
            changed = true;
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole(DEFAULT_ROLE);
            changed = true;
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
            changed = true;
        }
        if (user.getVerifyStatus() == null || user.getVerifyStatus().isBlank()) {
            user.setVerifyStatus(DEFAULT_VERIFY_STATUS);
            changed = true;
        }
        user.setLastLoginTime(now);
        changed = true;
        if (changed) {
            userMapper.updateById(user);
        }
    }

    private String buildDefaultNickname(String openid) {
        if (openid == null || openid.isBlank()) {
            return "wx_user";
        }
        String suffix = openid.length() <= 6 ? openid : openid.substring(openid.length() - 6);
        return "wx_user_" + suffix;
    }

    private String buildDefaultAvatar(String openid) {
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + openid;
    }

    private String normalizeRole(String role) {
        return role == null || role.isBlank() ? DEFAULT_ROLE : role;
    }

    private String asText(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
