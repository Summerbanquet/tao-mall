package com.tao.common.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 文件上传
     * @param file
     * @return
     */
    String upload(MultipartFile file);

    /**
     * 文件下载
     * @param response
     */
    void download(String fileName, HttpServletResponse response);

    boolean delete(String fileName);
}
