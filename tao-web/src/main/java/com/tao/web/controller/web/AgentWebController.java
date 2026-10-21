package com.tao.web.controller.web;

import com.tao.agent.service.AIChatService;
import com.tao.agent.service.AIImageService;
import com.tao.common.entity.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * 用户端 AI 接口：客服咨询对话（文本 + 图片，以流式为主）、产品生图。
 * 真正的模型调用由 tao-agent 模块的 AIChatService / AIImageService 实现。
 * 前缀 /api 与前端 Vite 代理（/api -> localhost:8080）对齐。
 */
@RestController
@RequestMapping("/api")
public class AgentWebController {

    @Autowired
    private AIChatService aiChatService;
    @Autowired
    private AIImageService aiImageService;

    /**
     * 客服咨询（非流式，支持文本 + 图片地址 + 当前用户ID）
     */
    @GetMapping("/consult")
    public AjaxResult consult(@RequestParam("question") String question,
                              @RequestParam(value = "imageUrl", required = false) String imageUrl,
                              @RequestParam(value = "userId", required = false) Long userId) {
        try {
            String answer = aiChatService.chat(question, imageUrl, userId);
            return AjaxResult.success(answer);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 客服咨询（流式 SSE，以流式对话为主，支持文本 + 图片地址 + 当前用户ID）
     */
    @GetMapping(value = "/consultStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter consultStream(@RequestParam("question") String question,
                                    @RequestParam(value = "imageUrl", required = false) String imageUrl,
                                    @RequestParam(value = "userId", required = false) Long userId) {
        Flux<String> flux = aiChatService.chatStream(question, imageUrl, userId);
        SseEmitter emitter = new SseEmitter(0L);
        flux.subscribe(
                chunk -> {
                    try {
                        emitter.send(chunk);
                    } catch (IOException ignored) {
                        emitter.complete();
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );
        return emitter;
    }

    /**
     * 产品生图：调用模型生成图片，自动下载后经 FileService 上传到本地，返回可用地址
     */
    @GetMapping("/genimage")
    public AjaxResult genimage(@RequestParam("description") String description) {
        try {
            String url = aiImageService.generateImage(description);
            return AjaxResult.success(url);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}
