package com.campustao.controller;

import com.campustao.common.result.R;
import com.campustao.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /** 上传 POST /api/file/upload，form-data 参数名 file */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        String fileName = fileService.upload(file);
        return R.ok("/uploads/" + fileName);   // 返回静态访问路径，前端直接当图片地址用
    }

    /** 下载 GET /api/file/download/{fileName} */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        fileService.download(fileName, response);
    }

    /** 删除 DELETE /api/file/delete/{fileName} */
    @DeleteMapping("/delete/{fileName}")
    public R<Boolean> delete(@PathVariable String fileName) {
        return R.ok(fileService.delete(fileName));
    }
}