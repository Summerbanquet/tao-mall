package com.tao.agent.service;

import reactor.core.publisher.Flux;

public interface AIChatService {

    /**
     * 文本对话（非流式）
     */
    String chat(String question);

    /**
     * 文本 + 图片对话（非流式）
     */
    String chat(String question, String imageUrl);

    /**
     * 文本 + 图片对话（非流式），并携带当前用户ID以便检索其订单/资料等系统数据
     */
    String chat(String question, String imageUrl, Long userId);

    /**
     * 文本对话（流式）
     */
    Flux<String> chatStream(String question);

    /**
     * 文本 + 图片对话（流式）
     */
    Flux<String> chatStream(String question, String imageUrl);

    /**
     * 文本 + 图片对话（流式），并携带当前用户ID以便检索其订单/资料等系统数据
     */
    Flux<String> chatStream(String question, String imageUrl, Long userId);
}
