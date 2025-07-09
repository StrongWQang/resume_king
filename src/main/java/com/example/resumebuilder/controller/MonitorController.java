package com.example.resumebuilder.controller;

import com.example.resumebuilder.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 服务器监控控制器
 */
@RestController
@RequestMapping("/api/monitor")
@CrossOrigin
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    /**
     * 获取服务器负载信息
     */
    @GetMapping("/server-info")
    public ResponseEntity<Map<String, Object>> getServerInfo() {
        try {
            Map<String, Object> serverInfo = monitorService.getServerInfo();
            return ResponseEntity.ok(serverInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "获取服务器信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取AI分析结果
     */
    @PostMapping("/ai-analysis")
    public ResponseEntity<Map<String, Object>> getAIAnalysis(@RequestBody Map<String, Object> serverInfo) {
        try {
            String analysis = monitorService.analyzeServerStatus(serverInfo);
            return ResponseEntity.ok(Map.of("analysis", analysis));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "获取AI分析失败: " + e.getMessage()));
        }
    }
} 