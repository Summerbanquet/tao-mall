package com.tao.web.controller.common;

import com.tao.common.entity.AjaxResult;
import com.tao.common.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件下载 / 删除（需登录，由 WebConfig 的 /web/common/** 拦截器保证）
 */
@RestController
@RequestMapping("/web/common")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 文件下载
     * @param fileName 上传后返回的文件名（形如 uuid.jpg，可带或不带 /uploads/ 前缀）
     */
    @GetMapping("/download")
    public void download(@RequestParam("fileName") String fileName, HttpServletResponse response) {
        fileService.download(fileName, response);
    }

    /**
     * 文件删除
     * @param fileName 上传后返回的文件名
     */
    @GetMapping("/delete")
    public AjaxResult delete(@RequestParam("fileName") String fileName) {
        boolean result = fileService.delete(fileName);
        return result ? AjaxResult.success("删除成功", true) : AjaxResult.error("删除失败", false);
    }
}
