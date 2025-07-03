package com.example.resumebuilder.service.impl;

import com.example.resumebuilder.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIServiceImpl implements AIService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String optimizeText(String text) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // 构建优化提示词
        String prompt = buildOptimizePrompt(text);

        // 构建请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("messages", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .put("role", "user")
                .put("content", prompt)));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        // 调用 DeepSeek API
        String response = restTemplate.postForObject(apiUrl, request, String.class);
        JsonNode responseJson = objectMapper.readTree(response);

        // 解析响应
        return responseJson.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText();
    }

    private String buildOptimizePrompt(String text) {
        return String.format("""
            你是一个专业的简历优化专家，请帮我优化以下简历内容，使其更加专业、有说服力，
            同时保持内容的真实性。优化时请注意：
            1. 使用更专业的表达方式
            2. 突出关键成就和数据
            3. 使用积极、主动的语气
            4. 保持简洁明了
            5. 确保语言流畅自然
            
            需要优化的内容：
            %s
            
            请直接返回优化后的内容，不要包含任何解释或其他文字。
            """, text);
    }
} 