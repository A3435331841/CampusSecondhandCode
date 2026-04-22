package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.LoginDTO;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_EXPIRE_HOURS = 72;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        // 从数据库查询用户（支持 username 登录）
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !user.getPassword().equals(loginDTO.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }

        // 生成 Token 并存储到 Redis，有效期 72 小时
        String token = UUID.randomUUID().toString().replace("-", "");
        String redisKey = TOKEN_PREFIX + token;
        // 存储 userId:role 格式
        stringRedisTemplate.opsForValue().set(redisKey,
                user.getId() + ":" + user.getRole(),
                TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("role", user.getRole());
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && !token.isEmpty()) {
            stringRedisTemplate.delete(TOKEN_PREFIX + token);
        }
        return Result.success("退出成功");
    }
}
