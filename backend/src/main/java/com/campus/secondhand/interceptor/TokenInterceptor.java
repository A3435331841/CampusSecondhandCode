package com.campus.secondhand.interceptor;

import com.campus.secondhand.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_PREFIX = "token:";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            writeUnauthorized(response, "请先登录");
            return false;
        }

        String redisKey = TOKEN_PREFIX + token;
        String userInfo = stringRedisTemplate.opsForValue().get(redisKey);
        if (userInfo == null) {
            writeUnauthorized(response, "登录已过期，请重新登录");
            return false;
        }

        // 解析 userId:role 写入请求属性，供 Controller 使用
        String[] parts = userInfo.split(":");
        request.setAttribute("currentUserId", Long.parseLong(parts[0]));
        request.setAttribute("currentUserRole", parts.length > 1 ? parts[1] : "user");
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(msg);
        result.setCode(401);
        response.getWriter().write(MAPPER.writeValueAsString(result));
    }
}
