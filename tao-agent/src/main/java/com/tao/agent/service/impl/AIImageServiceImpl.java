package com.tao.agent.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tao.agent.service.AIImageService;
import com.tao.common.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

/**
 * 通义千问（DashScope）文生图实现：采用基础 HttpURLConnection 调用，
 * 提交异步任务 -> 轮询结果 -> 下载图片 -> 经 FileService.upload 落库并返回可访问地址。
 */
@Service
public class AIImageServiceImpl implements AIImageService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private FileService fileService;

    @Value("${tao.ai.api-key:}")
    private String apiKey;
    @Value("${tao.ai.image.url:}")
    private String imageUrl;
    @Value("${tao.ai.image.task-url:}")
    private String taskUrl;
    @Value("${tao.ai.image.model:wanx2.1-t2i-turbo}")
    private String imageModel;

    @Override
    public String generateImage(String prompt) {
        if (isEmpty(apiKey)) {
            throw new RuntimeException("AI 服务未配置（请设置 tao.ai.api-key 或环境变量 AI_API_KEY）");
        }
        if (isEmpty(prompt)) {
            throw new RuntimeException("图片描述（prompt）不能为空");
        }
        try {
            // 1. 提交异步文生图任务
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", imageModel,
                    "input", Map.of("prompt", prompt),
                    "parameters", Map.of("size", "1024*1024", "n", 1)
            ));
            String submitResp = postJsonWithHeader(imageUrl, apiKey, body, Map.of("X-DashScope-Async", "enable"));
            JsonNode submit = objectMapper.readTree(submitResp);
            String taskId = submit.path("output").path("task_id").asText(null);
            if (isEmpty(taskId)) {
                throw new RuntimeException("提交文生图任务失败：" + submitResp);
            }

            // 2. 轮询任务直到完成
            String imageLink = pollTask(taskId);

            // 3. 下载图片字节
            byte[] bytes = download(imageLink);

            // 4. 通过 FileService 上传，返回可用地址
            String fileName = extractName(imageLink);
            MultipartFile mf = new ByteArrayMultipartFile(fileName, bytes);
            return fileService.upload(mf);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("生成图片失败：" + e.getMessage(), e);
        }
    }

    private String pollTask(String taskId) throws Exception {
        String url = taskUrl + taskId;
        int maxRetry = 30;
        for (int i = 0; i < maxRetry; i++) {
            String resp = getJsonWithHeader(url, apiKey);
            JsonNode root = objectMapper.readTree(resp);
            String status = root.path("output").path("task_status").asText("");
            if ("SUCCEEDED".equals(status)) {
                JsonNode results = root.path("output").path("results");
                if (results.isArray() && results.size() > 0) {
                    String link = results.get(0).path("url").asText(null);
                    if (!isEmpty(link)) {
                        return link;
                    }
                }
                throw new RuntimeException("任务成功但未返回图片地址：" + resp);
            }
            if ("FAILED".equals(status) || "UNKNOWN".equals(status)) {
                throw new RuntimeException("文生图任务失败：" + resp);
            }
            Thread.sleep(2000);
        }
        throw new RuntimeException("文生图任务超时（已轮询 " + maxRetry + " 次）");
    }

    private String postJsonWithHeader(String urlStr, String key, String body, Map<String, String> headers) throws IOException {
        HttpURLConnection conn = openPost(urlStr, key, body, headers);
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

    private HttpURLConnection openPost(String urlStr, String key, String body, Map<String, String> headers) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Authorization", "Bearer " + key);
        if (headers != null) {
            headers.forEach(conn::setRequestProperty);
        }
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return conn;
    }

    private String getJsonWithHeader(String urlStr, String key) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + key);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
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

    private String extractName(String link) {
        String s = link;
        int q = s.indexOf('?');
        if (q >= 0) {
            s = s.substring(0, q);
        }
        int slash = s.lastIndexOf('/');
        String name = slash >= 0 ? s.substring(slash + 1) : s;
        if (isEmpty(name) || name.indexOf('.') < 0) {
            name = "gen_" + System.currentTimeMillis() + ".png";
        }
        return name;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** 将下载到的字节包装为 MultipartFile，供 FileService.upload 使用（无需引入测试依赖） */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final String name;
        private final byte[] content;

        ByteArrayMultipartFile(String originalFilename, byte[] content) {
            this.name = originalFilename;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            int i = name.lastIndexOf('.');
            String ext = i >= 0 ? name.substring(i + 1).toLowerCase() : "png";
            return "image/" + ext;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }
}
