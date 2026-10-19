package com.tao.common.service.impl;

import com.tao.common.service.FileService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    /** 对外访问前缀，需与 WebConfig.addResourceHandlers 的 /uploads/** 映射保持一致 */
    private static final String URL_PREFIX = "/uploads";

    /** 允许的图片后缀 */
    private static final String IMAGE_EXTS = "jpg,jpeg,png,gif,webp,bmp";

    @Value("${file.upload.path}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("被上传文件不能为空");
        }
        String original = file.getOriginalFilename();
        if (original == null || original.indexOf('.') < 0) {
            throw new IllegalArgumentException("文件缺少扩展名");
        }
        String ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        if (!IMAGE_EXTS.contains(ext.substring(1))) {
            throw new IllegalArgumentException("仅支持图片格式：jpg/png/gif/webp/bmp");
        }
        String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;
        File dest = new File(uploadPath + File.separator + newFileName);
        try {
            file.transferTo(dest);
            return URL_PREFIX + "/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void download(String fileName, HttpServletResponse response) {
        // 防目录穿越：去掉访问前缀与上级目录符号
        String safe = fileName.replace("\\", "/").replace(URL_PREFIX + "/", "");
        safe = safe.replaceAll("\\.\\./", "").replaceAll("\\.\\.", "");
        File file = new File(uploadPath + File.separator + safe);
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("文件不存在：" + fileName);
        }
        String contentType;
        try {
            contentType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        String encodeName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        response.reset();
        response.setContentType(contentType);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentLengthLong(file.length());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodeName);

        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[2048];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败：" + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String fileName) {
        String safe = fileName.replace("\\", "/").replace(URL_PREFIX + "/", "");
        safe = safe.replaceAll("\\.\\./", "").replaceAll("\\.\\.", "");
        File file = new File(uploadPath + File.separator + safe);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }
}
