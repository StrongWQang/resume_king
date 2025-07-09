package com.example.resumebuilder.controller;

import com.example.resumebuilder.util.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控控制器
 * 提供系统监控信息
 */
@RestController
@RequestMapping("/api/monitor")
public class MonitorController {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitorController.class);
    
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;
    
    /**
     * 获取雪花算法ID生成器监控信息
     */
    @GetMapping("/snowflake")
    public Map<String, Object> getSnowflakeMonitor() {
        logger.info("获取雪花算法ID生成器监控信息");
        
        try {
            SnowflakeIdGenerator.MonitorInfo monitorInfo = snowflakeIdGenerator.getMonitorInfo();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", Map.of(
                "totalGenerated", monitorInfo.getTotalGenerated(),
                "clockBackwardCount", monitorInfo.getClockBackwardCount(),
                "waitStrategyCount", monitorInfo.getWaitStrategyCount(),
                "borrowStrategyCount", monitorInfo.getBorrowStrategyCount(),
                "clockBackwardDetected", monitorInfo.isClockBackwardDetected(),
                "datacenterId", monitorInfo.getDatacenterId(),
                "machineId", monitorInfo.getMachineId(),
                "systemTime", System.currentTimeMillis()
            ));
            
            return result;
        } catch (Exception e) {
            logger.error("获取雪花算法监控信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取监控信息失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 生成测试ID（用于测试雪花算法）
     */
    @GetMapping("/snowflake/test")
    public Map<String, Object> generateTestId() {
        logger.info("生成测试ID");
        
        try {
            long id = snowflakeIdGenerator.nextId();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", Map.of(
                "id", id,
                "stringId", String.valueOf(id),
                "timestamp", System.currentTimeMillis()
            ));
            
            return result;
        } catch (Exception e) {
            logger.error("生成测试ID失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "生成测试ID失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 批量生成测试ID（用于压力测试）
     */
    @GetMapping("/snowflake/batch-test")
    public Map<String, Object> generateBatchTestIds() {
        logger.info("批量生成测试ID");
        
        try {
            int batchSize = 100;
            long[] ids = new long[batchSize];
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < batchSize; i++) {
                ids[i] = snowflakeIdGenerator.nextId();
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", Map.of(
                "batchSize", batchSize,
                "duration", duration,
                "averageTime", duration / (double) batchSize,
                "firstId", ids[0],
                "lastId", ids[batchSize - 1],
                "monitorInfo", snowflakeIdGenerator.getMonitorInfo()
            ));
            
            return result;
        } catch (Exception e) {
            logger.error("批量生成测试ID失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "批量生成测试ID失败: " + e.getMessage());
            return result;
        }
    }
} 