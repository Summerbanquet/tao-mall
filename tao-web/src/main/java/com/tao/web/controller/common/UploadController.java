package com.tao.web.controller.common;

import com.tao.common.entity.AjaxResult;
import com.tao.web.interceptor.LoginInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用上传接口（已加入 WebConfig 拦截器，需登录）
 * 上传成功后返回的图片 URL 形如 /uploads/2026-10-16/xxx.jpg，
 * 由 WebConfig 的 addResourceHandlers 映射到 file.upload.path 目录
 */
@RestController
@RequestMapping("/web/common")
public class UploadController {

    /** 允许的图片后缀 */
    private static final String IMAGE_EXTS = "jpg,jpeg,png,gif,webp,bmp";

    @Value("${file.upload.path}")
    private String uploadPath;

    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 拦截器会保证 token，但为了跨域或意外放行再校验一次
        if (request.getAttribute(LoginInterceptor.USER_NAME_ATTR) == null) {
            return AjaxResult.error("请先登录");
        }
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }
        String original = file.getOriginalFilename();
        if (original == null) {
            return AjaxResult.error("文件名不合法");
        }
        int dotIdx = original.lastIndexOf('.');
        if (dotIdx < 0) {
            return AjaxResult.error("文件缺少扩展名");
        }
        String ext = original.substring(dotIdx).toLowerCase();
        if (IMAGE_EXTS.contains(ext.substring(1)) == false) {
            return AjaxResult.error("仅支持图片格式：jpg/png/gif/webp/bmp");
        }

        // 按日期分目录：上传根/2026-10-16/uuid.jpg
        String dateDir = LocalDate.now().toString();
        File dir = new File(uploadPath, dateDir);
        if (!dir.exists() && !dir.mkdirs()) {
            return AjaxResult.error("上传目录创建失败：" + dir.getAbsolutePath());
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        File dest = new File(dir, filename);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
            return AjaxResult.error("保存失败：" + e.getMessage());
        }

        String url = "/uploads/" + dateDir + "/" + filename;
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        data.put("name", original);
        return AjaxResult.success("上传成功", data);
    }
}