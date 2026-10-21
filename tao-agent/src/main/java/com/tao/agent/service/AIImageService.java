package com.tao.agent.service;

public interface AIImageService {

    /**
     * 文生图：调用模型生成图片，自动下载后经 FileService 上传到本地存储，返回可访问地址。
     *
     * @param prompt 图片描述文本
     * @return 可访问的图片地址，如 /uploads/xxxx.png
     */
    String generateImage(String prompt);
}
