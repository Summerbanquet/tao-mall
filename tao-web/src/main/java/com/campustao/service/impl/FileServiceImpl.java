package com.campustao.service.impl;

import com.campustao.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String original = file.getOriginalFilename();
// 客户端可能不带原始文件名，判空防止NPE；无后缀时扩展名给空串
        int dot = original != null ? original.lastIndexOf('.') : -1;
        String ext = dot >= 0 ? original.substring(dot) : "";

        // UUID重命名防止重名互相覆盖
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Path dir = Paths.get(uploadPath);
            Files.createDirectories(dir);            // 目录不存在则自动创建
            file.transferTo(dir.resolve(fileName));  // 临时文件转存到磁盘
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }

    @Override
    public void download(String fileName, HttpServletResponse response) {
        checkFileName(fileName);
        Path path = Paths.get(uploadPath).resolve(fileName);
        if (!Files.exists(path)) {
            throw new RuntimeException("文件不存在");
        }
        try {
            String type = URLConnection.guessContentTypeFromName(fileName);
            response.setContentType(type != null ? type : "application/octet-stream");
            // attachment 表示弹框下载；编码防止中文文件名乱码
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentLengthLong(Files.size(path));
            Files.copy(path, response.getOutputStream());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("文件名编码失败", e);
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    public boolean delete(String fileName) {
        checkFileName(fileName);
        try {
            return Files.deleteIfExists(Paths.get(uploadPath).resolve(fileName));
        } catch (IOException e) {
            throw new RuntimeException("文件删除失败", e);
        }
    }

    /** 防路径穿越：禁止 .. / / \ 出现在文件名里 */
    private void checkFileName(String fileName) {
        if (!StringUtils.hasText(fileName) || fileName.contains("..")
                || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("非法文件名");
        }
    }
}