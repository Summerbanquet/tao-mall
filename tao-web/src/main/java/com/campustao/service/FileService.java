package com.campustao.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 文件服务：上传 / 下载 / 删除（本地磁盘存储）
 */
public interface FileService {

    /** 上传，返回存储文件名 */
    String upload(MultipartFile file);

    /** 下载，文件流写出到响应 */
    void download(String fileName, HttpServletResponse response);

    /** 删除磁盘文件 */
    boolean delete(String fileName);
}