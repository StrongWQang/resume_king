package com.example.resumebuilder.service;

import java.util.Map;

/**
 * 监控服务接口
 */
public interface MonitorService {
    
    /**
     * 获取服务器负载信息
     * @return 服务器负载信息
     */
    Map<String, Object> getServerInfo();
    
    /**
     * 分析服务器状态
     * @param serverInfo 服务器信息
     * @return AI分析结果
     */
    String analyzeServerStatus(Map<String, Object> serverInfo);
} 