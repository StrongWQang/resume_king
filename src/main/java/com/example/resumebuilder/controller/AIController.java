package com.example.resumebuilder.controller;

import com.example.resumebuilder.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class AIController {

    @Autowired
    private AIService aiService;

    // 限流：记录每个IP最后一次请求时间
    private static final ConcurrentHashMap<String, Long> ipRequestTimeMap = new ConcurrentHashMap<>();
    private static final long RATE_LIMIT_INTERVAL_MS = 5000; // 5秒

    @PostMapping("/optimize")
    public ResponseEntity<?> optimizeText(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        // 获取请求IP并进行限流检查
        String ip = httpRequest.getRemoteAddr();
        long now = System.currentTimeMillis();
        Long lastTime = ipRequestTimeMap.get(ip);
        if (lastTime != null && now - lastTime < RATE_LIMIT_INTERVAL_MS) {
            return ResponseEntity.status(429).body(Map.of("error", "AI优化请求过于频繁，请等待5秒后再试"));
        }
        ipRequestTimeMap.put(ip, now);

        try {
            String text = request.get("text");
            String optimizedText = aiService.optimizeText(text);
            return ResponseEntity.ok(Map.of("optimizedText", optimizedText));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 