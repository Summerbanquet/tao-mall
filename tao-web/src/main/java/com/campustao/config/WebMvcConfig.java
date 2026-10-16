package com.campustao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 把本地磁盘目录映射为静态资源URL：/uploads/** → file:${file.upload.path}/
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 两个易错点：file: 前缀必须有；目录结尾必须有 /
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}