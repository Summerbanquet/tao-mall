package com.tao.web.config;

import com.tao.web.interceptor.AdminInterceptor;
import com.tao.web.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：拦截需要登录的接口 + 配置上传图片的静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 需要登录的接口
                .addPathPatterns(
                        "/web/user/info",
                        "/web/user/logout",
                        "/web/order/**",
                        "/web/goods/add",
                        "/web/goods/edit",
                        "/web/goods/my",
                        "/web/goods/status/**",
                        "/web/goods/dele/**",
                        "/web/collect/**",
                        "/web/common/**",
                        "/adm/**"
                )
                // 排除放行的接口
                .excludePathPatterns(
                        "/web/user/login",
                        "/web/user/register",
                        "/web/user/checkUserName/**"
                );

        // 管理端接口：在登录校验之后，再校验是否为管理员（type=1）
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/adm/**");
    }

    /**
     * 将 /uploads/** 映射到 file.upload.path 配置的目录，
     * 上传后的图片可直接通过 http://host:8080/uploads/... 访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadPath.replace("\\", "/") + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}