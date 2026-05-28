package com.campus.secondhand.config;

import com.campus.secondhand.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/wechat/login")
                .excludePathPatterns("/api/product/list", "/api/product/detail/**")
                .excludePathPatterns("/api/category/list")
                .excludePathPatterns("/api/comment/list")
                .excludePathPatterns("/api/file/img/**")
                .excludePathPatterns("/api/ai/**")
                .excludePathPatterns("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absPath = Path.of(uploadDir).toAbsolutePath().toString().replace('\\', '/');
        if (!absPath.endsWith("/")) absPath += "/";
        registry.addResourceHandler("/api/file/img/**")
                .addResourceLocations("file:" + absPath);
    }
}
