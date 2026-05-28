package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.LoginDTO;
import com.campus.secondhand.dto.WechatLoginDTO;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.WechatAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_EXPIRE_HOURS = 72;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private WechatAuthService wechatAuthService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        if (loginDTO == null || loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            return Result.error("请输入账号和密码");
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null || user.getPassword() == null
                || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.error("账号或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        String role = normalizeRole(user.getRole());
        String token = UUID.randomUUID().toString().replace("-", "");
        try {
            stringRedisTemplate.opsForValue().set(
                    TOKEN_PREFIX + token,
                    user.getId() + ":" + role,
                    TOKEN_EXPIRE_HOURS,
                    TimeUnit.HOURS
            );
        } catch (Exception e) {
            return Result.error("登录服务暂不可用，请确认 Redis 已启动");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("role", role);
        data.put("verifyStatus", user.getVerifyStatus());
        return Result.success(data);
    }

    @PostMapping("/wechat/login")
    public Result<Map<String, Object>> wechatLogin(@RequestBody WechatLoginDTO loginDTO) {
        try {
            return Result.success(wechatAuthService.login(loginDTO));
        } catch (IllegalStateException exception) {
            return Result.error(exception.getMessage());
        } catch (Exception exception) {
            return Result.error("微信登录失败");
        }
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && !token.isEmpty()) {
            stringRedisTemplate.delete(TOKEN_PREFIX + token);
        }
        return Result.success("已退出登录");
    }

    private String normalizeRole(String role) {
        return role == null || role.isBlank() ? "user" : role;
    }
}
