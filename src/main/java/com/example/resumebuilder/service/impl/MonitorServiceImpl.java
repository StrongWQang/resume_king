package com.example.resumebuilder.service.impl;

import com.example.resumebuilder.service.MonitorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 监控服务实现类
 */
@Service
public class MonitorServiceImpl implements MonitorService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);
    
    @Value("${deepseek.api.key:}")
    private String deepSeekApiKey;
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String deepSeekApiUrl;

    @Override
    public Map<String, Object> getServerInfo() {
        Map<String, Object> serverInfo = new HashMap<>();
        
        try {
            // 获取CPU使用率
            double cpuUsage = getCpuUsage();
            serverInfo.put("cpu", Math.round(cpuUsage * 100.0) / 100.0);
            
            // 获取内存使用率
            double memoryUsage = getMemoryUsage();
            serverInfo.put("memory", Math.round(memoryUsage * 100.0) / 100.0);
            
            // 获取磁盘使用率
            double diskUsage = getDiskUsage();
            serverInfo.put("disk", Math.round(diskUsage * 100.0) / 100.0);
            
            // 添加时间戳
            serverInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            logger.info("获取服务器信息成功: CPU={}%, Memory={}%, Disk={}%", 
                       cpuUsage, memoryUsage, diskUsage);
            
        } catch (Exception e) {
            logger.error("获取服务器信息失败", e);
            throw new RuntimeException("获取服务器信息失败", e);
        }
        
        return serverInfo;
    }

    @Override
    public String analyzeServerStatus(Map<String, Object> serverInfo) {
        if (deepSeekApiKey == null || deepSeekApiKey.isEmpty()) {
            logger.warn("DeepSeek API Key 未配置，返回模拟分析结果");
            return generateMockAnalysis(serverInfo);
        }
        
        try {
            // 构建发送给DeepSeek的消息
            String prompt = buildAnalysisPrompt(serverInfo);
            
            // 调用DeepSeek API
            String response = callDeepSeekApi(prompt);
            
            logger.info("DeepSeek 分析完成");
            return response;
            
        } catch (Exception e) {
            logger.error("调用DeepSeek API失败", e);
            // 如果API调用失败，返回模拟分析结果
            return generateMockAnalysis(serverInfo);
        }
    }

    /**
     * 获取CPU使用率
     */
    private double getCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            // 如果是com.sun.management.OperatingSystemMXBean，可以获取更详细的信息
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
                double cpuUsage = sunOsBean.getProcessCpuLoad() * 100;
                return cpuUsage > 0 ? cpuUsage : getSystemCpuUsage();
            }
            
            // 降级方案：通过系统负载计算
            double systemLoadAverage = osBean.getSystemLoadAverage();
            int availableProcessors = osBean.getAvailableProcessors();
            
            if (systemLoadAverage > 0) {
                return Math.min((systemLoadAverage / availableProcessors) * 100, 100.0);
            }
            
            return getSystemCpuUsage();
            
        } catch (Exception e) {
            logger.warn("获取CPU使用率失败，使用默认值", e);
            return Math.random() * 30 + 20; // 返回20-50%的随机值作为示例
        }
    }

    /**
     * 通过系统命令获取CPU使用率
     */
    private double getSystemCpuUsage() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String command;
            
            if (os.contains("win")) {
                // Windows系统
                command = "wmic cpu get loadpercentage /value";
            } else {
                // Linux/Mac系统
                command = "top -bn1 | grep 'Cpu(s)' | sed 's/.*, *\\([0-9.]*\\)%* id.*/\\1/' | awk '{print 100 - $1}'";
            }
            
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("LoadPercentage")) {
                    String[] parts = line.split("=");
                    if (parts.length > 1) {
                        return Double.parseDouble(parts[1].trim());
                    }
                } else if (line.trim().matches("\\d+\\.?\\d*")) {
                    return Double.parseDouble(line.trim());
                }
            }
            
        } catch (Exception e) {
            logger.warn("通过系统命令获取CPU使用率失败", e);
        }
        
        return Math.random() * 30 + 20; // 返回默认值
    }

    /**
     * 获取内存使用率
     */
    private double getMemoryUsage() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() + 
                             memoryBean.getNonHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax() + 
                            memoryBean.getNonHeapMemoryUsage().getMax();
            
            if (maxMemory > 0) {
                return (double) usedMemory / maxMemory * 100;
            }
            
            // 降级方案：获取系统内存
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
                
                long totalMemory = sunOsBean.getTotalPhysicalMemorySize();
                long freeMemory = sunOsBean.getFreePhysicalMemorySize();
                long usedSystemMemory = totalMemory - freeMemory;
                
                return (double) usedSystemMemory / totalMemory * 100;
            }
            
        } catch (Exception e) {
            logger.warn("获取内存使用率失败，使用默认值", e);
        }
        
        return Math.random() * 40 + 30; // 返回30-70%的随机值作为示例
    }

    /**
     * 获取磁盘使用率
     */
    private double getDiskUsage() {
        try {
            Path rootPath = FileSystems.getDefault().getPath("/");
            FileStore store = FileSystems.getDefault().getFileStores().iterator().next();
            
            long totalSpace = store.getTotalSpace();
            long usableSpace = store.getUsableSpace();
            long usedSpace = totalSpace - usableSpace;
            
            return (double) usedSpace / totalSpace * 100;
            
        } catch (Exception e) {
            logger.warn("获取磁盘使用率失败，使用默认值", e);
            return Math.random() * 30 + 40; // 返回40-70%的随机值作为示例
        }
    }

    /**
     * 构建分析提示词
     */
    private String buildAnalysisPrompt(Map<String, Object> serverInfo) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为一名专业的系统运维专家，请分析以下服务器状态并给出建议：\n\n");
        prompt.append("服务器当前状态：\n");
        prompt.append(String.format("- CPU使用率: %.2f%%\n", serverInfo.get("cpu")));
        prompt.append(String.format("- 内存使用率: %.2f%%\n", serverInfo.get("memory")));
        prompt.append(String.format("- 磁盘使用率: %.2f%%\n", serverInfo.get("disk")));
        prompt.append(String.format("- 检测时间: %s\n\n", serverInfo.get("timestamp")));
        
        prompt.append("请从以下角度进行分析：\n");
        prompt.append("1. 当前服务器整体健康状况评估\n");
        prompt.append("2. 是否存在性能瓶颈或潜在问题\n");
        prompt.append("3. 给出具体的优化建议\n");
        prompt.append("4. 如果有异常，请指出可能的原因\n\n");
        prompt.append("请用中文回答，内容简洁明了，重点突出。");
        
        return prompt.toString();
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekApi(String prompt) throws Exception {
        URL url = new URL(deepSeekApiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + deepSeekApiKey);
        connection.setDoOutput(true);
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("messages", new Object[]{
            Map.of("role", "user", "content", prompt)
        });
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);
        
        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(requestBody);
        
        // 发送请求
        connection.getOutputStream().write(requestJson.getBytes("UTF-8"));
        
        // 读取响应
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            // 解析响应
            Map<String, Object> responseMap = mapper.readValue(response.toString(), Map.class);
            Map<String, Object> firstChoice = (Map<String, Object>) ((java.util.List<?>) responseMap.get("choices")).get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            
            return (String) message.get("content");
        } else {
            throw new RuntimeException("DeepSeek API 调用失败，状态码: " + responseCode);
        }
    }

    /**
     * 生成模拟分析结果
     */
    private String generateMockAnalysis(Map<String, Object> serverInfo) {
        StringBuilder analysis = new StringBuilder();
        
        double cpu = ((Number) serverInfo.get("cpu")).doubleValue();
        double memory = ((Number) serverInfo.get("memory")).doubleValue();
        double disk = ((Number) serverInfo.get("disk")).doubleValue();
        
        analysis.append("🔍 服务器状态分析报告\n\n");
        
        // 整体健康状况评估
        analysis.append("📊 整体健康状况：");
        if (cpu < 60 && memory < 70 && disk < 80) {
            analysis.append("良好 ✅\n");
        } else if (cpu < 80 && memory < 80 && disk < 90) {
            analysis.append("警告 ⚠️\n");
        } else {
            analysis.append("严重 ❌\n");
        }
        
        analysis.append("\n🔧 详细分析：\n");
        
        // CPU分析
        if (cpu < 50) {
            analysis.append("• CPU负载正常，系统运行流畅\n");
        } else if (cpu < 80) {
            analysis.append("• CPU负载较高，建议关注进程占用情况\n");
        } else {
            analysis.append("• CPU负载过高，可能影响系统响应速度\n");
        }
        
        // 内存分析
        if (memory < 60) {
            analysis.append("• 内存使用正常，有充足的可用空间\n");
        } else if (memory < 80) {
            analysis.append("• 内存使用率较高，建议监控内存泄漏\n");
        } else {
            analysis.append("• 内存使用率过高，建议及时释放或增加内存\n");
        }
        
        // 磁盘分析
        if (disk < 70) {
            analysis.append("• 磁盘空间充足，无需担心\n");
        } else if (disk < 90) {
            analysis.append("• 磁盘使用率较高，建议清理不必要的文件\n");
        } else {
            analysis.append("• 磁盘空间不足，建议立即清理或扩容\n");
        }
        
        analysis.append("\n💡 优化建议：\n");
        
        if (cpu > 70) {
            analysis.append("• 检查CPU密集型进程，考虑优化算法或增加服务器资源\n");
        }
        
        if (memory > 70) {
            analysis.append("• 监控内存使用情况，及时回收不必要的对象\n");
        }
        
        if (disk > 80) {
            analysis.append("• 定期清理日志文件和临时文件\n");
            analysis.append("• 考虑数据归档或磁盘扩容\n");
        }
        
        if (cpu < 50 && memory < 60 && disk < 70) {
            analysis.append("• 服务器运行状态良好，继续保持监控\n");
        }
        
        return analysis.toString();
    }
} 