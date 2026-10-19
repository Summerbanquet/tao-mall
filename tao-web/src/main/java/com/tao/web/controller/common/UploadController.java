package com.tao.web.controller.common;

import com.tao.common.entity.AjaxResult;
import com.tao.common.service.FileService;
import com.tao.web.interceptor.LoginInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用上传接口（需登录，由 WebConfig 的 /web/common/** 拦截器保证）
 * 上传成功后返回的图片 URL 形如 /uploads/xxx.jpg，
 * 由 WebConfig 的 addResourceHandlers 映射到 file.upload.path 目录，可直接访问
 */
@RestController
@RequestMapping("/web/common")
public class UploadController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (request.getAttribute(LoginInterceptor.USER_NAME_ATTR) == null) {
            return AjaxResult.error("请先登录");
        }
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }
        try {
            String url = fileService.upload(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("name", file.getOriginalFilename());
            return AjaxResult.success("上传成功", data);
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}
