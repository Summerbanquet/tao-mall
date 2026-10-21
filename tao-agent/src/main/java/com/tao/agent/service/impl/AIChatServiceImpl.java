package com.tao.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tao.agent.service.AIChatService;
import com.tao.goods.entity.Goods;
import com.tao.goods.mapper.GoodsMapper;
import com.tao.order.entity.Order;
import com.tao.order.mapper.OrderMapper;
import com.tao.user.entity.User;
import com.tao.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 通义千问（DashScope）对话实现：采用最基础的 HttpURLConnection 发起请求，不依赖任何 SDK；
 * 流式对话通过 reactor Flux 包装 SSE 数据流。
 * 作为“校园淘”智能客服，会按用户问题检索商品/订单/用户等系统数据并注入上下文。
 */
@Service
public class AIChatServiceImpl implements AIChatService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${tao.ai.api-key:}")
    private String apiKey;
    @Value("${tao.ai.chat.url:}")
    private String chatUrl;
    @Value("${tao.ai.chat.model:qwen-plus}")
    private String chatModel;

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    private static final String SYSTEM_PROMPT =
            "你是“校园淘”校园二手交易平台的智能客服助手。请用简洁、友好、口语化的中文回答用户关于" +
            "平台使用、商品、订单、交易流程的问题；你可以基于系统提供的【上下文数据】作答。" +
            "若用户的问题超出平台客服范畴（如涉及账号密码等隐私、或与校园二手交易无关），" +
            "或你无法获取所需数据，请礼貌说明，并提示“如需进一步帮助，可转接人工客服”。" +
            "不要编造不存在的商品或订单信息。";

    @Override
    public String chat(String question) {
        return chat(question, null, null);
    }

    @Override
    public String chat(String question, String imageUrl) {
        return chat(question, imageUrl, null);
    }

    @Override
    public String chat(String question, String imageUrl, Long userId) {
        if (isEmpty(apiKey)) {
            return "AI 服务未配置（请设置 tao.ai.api-key 或环境变量 AI_API_KEY）";
        }
        try {
            String context = buildContext(question, userId);
            String resp = postJson(chatUrl, apiKey, buildBody(question, imageUrl, context, false));
            JsonNode root = objectMapper.readTree(resp);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText("");
            }
            if (root.path("error").isObject()) {
                return "模型返回错误：" + root.path("error").path("message").asText(resp);
            }
            return "未获取到回复";
        } catch (Exception e) {
            return "对话失败：" + e.getMessage();
        }
    }

    @Override
    public Flux<String> chatStream(String question) {
        return chatStream(question, null, null);
    }

    @Override
    public Flux<String> chatStream(String question, String imageUrl) {
        return chatStream(question, imageUrl, null);
    }

    @Override
    public Flux<String> chatStream(String question, String imageUrl, Long userId) {
        return Flux.create(sink -> {
            if (isEmpty(apiKey)) {
                sink.next("AI 服务未配置（请设置 tao.ai.api-key 或环境变量 AI_API_KEY）。\n如需帮助，请转接人工客服。");
                sink.complete();
                return;
            }
            HttpURLConnection conn = null;
            try {
                String context = buildContext(question, userId);
                conn = openPost(chatUrl, apiKey, buildBody(question, imageUrl, context, true));
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("data:")) {
                            continue;
                        }
                        String data = line.substring(5).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        try {
                            JsonNode node = objectMapper.readTree(data);
                            if (node.path("error").isObject()) {
                                sink.error(new RuntimeException("模型返回错误：" + node.path("error").path("message").asText()));
                                return;
                            }
                            JsonNode delta = node.path("choices").path(0).path("delta").path("content");
                            if (!delta.isMissingNode() && delta.asText() != null && !delta.asText().isEmpty()) {
                                sink.next(delta.asText());
                            }
                        } catch (Exception ignored) {
                            // 跳过无法解析的数据行
                        }
                    }
                    sink.complete();
                }
            } catch (Exception e) {
                sink.error(e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    /** 依据问题关键词检索系统数据，拼为模型可用的上下文 */
    private String buildContext(String question, Long userId) {
        StringBuilder sb = new StringBuilder();
        if (containsAny(question, "商品", "卖", "找", "推荐", "有卖", "想买", "有什么", "在售", "闲置")) {
            List<Goods> goods = goodsMapper.selectList(
                    new QueryWrapper<Goods>().eq("status", Goods.STATUS_ON)
                            .orderByDesc("create_time").last("limit 6"));
            if (goods != null && !goods.isEmpty()) {
                sb.append("【平台在售商品参考】\n");
                for (Goods g : goods) {
                    sb.append("- ").append(g.getTitle()).append(" ￥").append(g.getPrice()).append("\n");
                }
            }
        }
        if (containsAny(question, "订单", "我的订单")) {
            if (userId != null) {
                List<Order> orders = orderMapper.selectList(
                        new QueryWrapper<Order>().eq("user_id", userId).last("limit 5"));
                if (orders != null && !orders.isEmpty()) {
                    sb.append("【该用户订单】\n");
                    for (Order o : orders) {
                        sb.append("- 订单号").append(o.getOrderNo())
                                .append(" 商品:").append(o.getGoodsName())
                                .append(" 状态:").append(orderStatus(o.getStatus())).append("\n");
                    }
                }
            } else {
                sb.append("【提示】用户未登录或未提供用户ID，无法查询其订单。\n");
            }
        }
        if (userId != null && containsAny(question, "我", "我的", "账号", "资料", "信息")) {
            User u = userMapper.selectById(userId);
            if (u != null) {
                sb.append("【当前用户】用户名:").append(u.getUserName())
                        .append(" 昵称:").append(u.getNickName() == null ? "" : u.getNickName()).append("\n");
            }
        }
        return sb.toString();
    }

    private String orderStatus(Integer s) {
        if (s == null) return "未知";
        switch (s) {
            case 0: return "待付款";
            case 1: return "已付款";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知(" + s + ")";
        }
    }

    private boolean containsAny(String text, String... keys) {
        if (text == null) return false;
        for (String k : keys) {
            if (text.contains(k)) return true;
        }
        return false;
    }

    /** 构建请求体：system 提示 + user 内容（文本及可选图片），并附带系统数据上下文 */
    private String buildBody(String question, String imageUrl, String context, boolean stream) throws IOException {
        List<Object> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        String text = (context != null && !context.isEmpty())
                ? (context + "\n\n用户问题：" + question)
                : question;
        Object content;
        String resolvedImage = resolveImage(imageUrl);
        if (resolvedImage != null) {
            List<Object> contentArr = new ArrayList<>();
            contentArr.add(Map.of("type", "image_url", "image_url", Map.of("url", resolvedImage)));
            contentArr.add(Map.of("type", "text", "text", text));
            content = contentArr;
        } else {
            content = text;
        }
        messages.add(Map.of("role", "user", "content", content));

        Map<String, Object> req = Map.of(
                "model", chatModel,
                "stream", stream,
                "messages", messages
        );
        return objectMapper.writeValueAsString(req);
    }

    // ===== HTTP 工具（简单 HTTP 调用模型） =====

    private HttpURLConnection openPost(String urlStr, String key, String body) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Authorization", "Bearer " + key);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(0); // 流式读取不设超时
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return conn;
    }

    private String postJson(String urlStr, String key, String body) throws IOException {
        HttpURLConnection conn = openPost(urlStr, key, body);
        int code = conn.getResponseCode();
        try (InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** 将图片地址解析为模型可读取形式：本地/相对地址由服务端下载并转 base64，外链原样返回 */
    private String resolveImage(String imageUrl) {
        if (isEmpty(imageUrl)) return null;
        try {
            String abs = imageUrl;
            if (abs.startsWith("/")) abs = "http://localhost:8080" + abs;
            boolean local = abs.contains("localhost") || abs.startsWith("http://127.")
                    || abs.startsWith("http://192.168.") || abs.startsWith("http://10.");
            if (local) {
                byte[] bytes = download(abs);
                String mime = guessMime(imageUrl);
                return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
            }
            return imageUrl;
        } catch (Exception e) {
            return imageUrl;
        }
    }

    private String guessMime(String name) {
        if (name == null) return "image/png";
        String n = name.toLowerCase();
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".gif")) return "image/gif";
        if (n.endsWith(".webp")) return "image/webp";
        if (n.endsWith(".bmp")) return "image/bmp";
        return "image/png";
    }

    private byte[] download(String link) throws IOException {
        URL url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);
        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        } finally {
            conn.disconnect();
        }
    }
}
