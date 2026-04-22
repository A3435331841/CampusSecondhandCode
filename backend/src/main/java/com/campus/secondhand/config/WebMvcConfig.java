package com.campus.secondhand.config;

import com.campus.secondhand.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/**")
                // 登录/登出不需要Token
                .excludePathPatterns("/api/auth/login")
                // 游客可浏览商品列表和详情
                .excludePathPatterns("/api/product/list", "/api/product/detail/**")
                // 静态资源
                .excludePathPatterns("/api/file/**")
                // OpenAPI 文档
                .excludePathPatterns("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将上传的图片映射为静态资源可访问
        registry.addResourceHandler("/api/file/img/**")
                .addResourceLocations("file:uploads/");
    }
}
