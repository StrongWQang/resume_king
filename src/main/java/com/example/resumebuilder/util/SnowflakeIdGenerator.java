package com.example.resumebuilder.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花算法ID生成器
 * 支持时钟回拨处理、等待策略、借号方案和监控告警
 */
@Component
public class SnowflakeIdGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerator.class);
    
    // 雪花算法参数
    private static final long START_TIMESTAMP = 1609459200000L; // 2021-01-01 00:00:00
    private static final long SEQUENCE_BIT = 12; // 序列号占用位数
    private static final long MACHINE_BIT = 5;   // 机器标识占用位数
    private static final long DATACENTER_BIT = 5; // 数据中心标识占用位数
    
    // 各部分最大值
    private static final long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    
    // 各部分位移
    private static final long MACHINE_LEFT = SEQUENCE_BIT;
    private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;
    
    // 配置参数
    @Value("${snowflake.datacenter-id:1}")
    private long datacenterId;
    
    @Value("${snowflake.machine-id:0}")
    private long machineId;
    
    @Value("${snowflake.clock-backward-wait-time:5000}")
    private long clockBackwardWaitTime; // 时钟回拨等待时间(ms)
    
    @Value("${snowflake.max-clock-backward-time:1000}")
    private long maxClockBackwardTime; // 最大允许时钟回拨时间(ms)
    
    // 运行时变量
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    // 时钟回拨处理相关
    private volatile boolean clockBackwardDetected = false;
    private final AtomicLong borrowedSequence = new AtomicLong(0);
    private long borrowedTimestamp = 0;
    
    // 监控统计
    private final AtomicLong totalGenerated = new AtomicLong(0);
    private final AtomicLong clockBackwardCount = new AtomicLong(0);
    private final AtomicLong waitStrategyCount = new AtomicLong(0);
    private final AtomicLong borrowStrategyCount = new AtomicLong(0);
    
    @PostConstruct
    public void init() {
        // 自动获取机器ID（如果未配置）
        if (machineId == 0) {
            machineId = generateMachineId();
        }
        
        // 验证配置参数
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenter Id can't be greater than " + MAX_DATACENTER_NUM + " or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machine Id can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        
        logger.info("雪花算法ID生成器初始化完成 - datacenterId: {}, machineId: {}", datacenterId, machineId);
    }
    
    /**
     * 分析雪花ID的组成部分
     */
    public static void analyzeId(long id) {
        long timestamp = (id >> TIMESTAMP_LEFT) + START_TIMESTAMP;
        long datacenterId = (id >> DATACENTER_LEFT) & MAX_DATACENTER_NUM;
        long machineId = (id >> MACHINE_LEFT) & MAX_MACHINE_NUM;
        long sequence = id & MAX_SEQUENCE;
        
        logger.info("ID分析 - ID: {}", id);
        logger.info("时间戳: {} ({})", timestamp, new java.util.Date(timestamp));
        logger.info("数据中心ID: {}", datacenterId);
        logger.info("机器ID: {}", machineId);
        logger.info("序列号: {}", sequence);
        logger.info("二进制形式: {}", String.format("%64s", Long.toBinaryString(id)).replace(' ', '0'));
    }

    /**
     * 生成下一个ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        
        // 检测时钟回拨
        if (timestamp < lastTimestamp) {
            handleClockBackward(timestamp);
        }
        
        // 同一毫秒内生成
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 序列号用完，等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 新的毫秒，使用随机起始序列号，但确保不超过最大值
            sequence = ThreadLocalRandom.current().nextLong(0, MAX_SEQUENCE);
        }
        
        lastTimestamp = timestamp;
        totalGenerated.incrementAndGet();
        
        // 构造ID并验证
        long id = ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT)
                | (datacenterId << DATACENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
                
        // 验证生成的ID是否合法
        if (id < 0) {
            logger.error("生成了负数ID: {}, timestamp: {}, datacenter: {}, machine: {}, sequence: {}", 
                id, timestamp, datacenterId, machineId, sequence);
            // 紧急处理：重新生成一个正数ID
            sequence = ThreadLocalRandom.current().nextLong(0, MAX_SEQUENCE / 2);
            id = ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT)
                | (datacenterId << DATACENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
        }
        
        return id;
    }
    
    /**
     * 处理时钟回拨
     */
    private void handleClockBackward(long currentTimestamp) {
        long backwardTime = lastTimestamp - currentTimestamp;
        clockBackwardCount.incrementAndGet();
        clockBackwardDetected = true;
        
        // 上报监控告警
        reportClockBackwardAlert(backwardTime);
        
        logger.warn("检测到时钟回拨 {} ms，当前时间: {}, 上次时间: {}", 
                backwardTime, currentTimestamp, lastTimestamp);
        
        if (backwardTime <= maxClockBackwardTime) {
            // 策略1：等待策略 - 回拨时间较小时等待
            waitForTimeRecovery(backwardTime);
            waitStrategyCount.incrementAndGet();
        } else {
            // 策略2：借号方案 - 回拨时间较大时使用借号
            useBorrowedSequence();
            borrowStrategyCount.incrementAndGet();
        }
    }
    
    /**
     * 等待策略：等待时间恢复
     */
    private void waitForTimeRecovery(long backwardTime) {
        try {
            logger.info("采用等待策略处理时钟回拨，等待 {} ms", backwardTime);
            Thread.sleep(Math.min(backwardTime + 1, clockBackwardWaitTime));
            
            long newTimestamp = System.currentTimeMillis();
            if (newTimestamp < lastTimestamp) {
                // 等待后仍然回拨，切换到借号方案
                logger.warn("等待策略失败，切换到借号方案");
                useBorrowedSequence();
                borrowStrategyCount.incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("等待时间恢复被中断", e);
        }
    }
    
    /**
     * 借号方案：使用预分配的序列号
     */
    private void useBorrowedSequence() {
        logger.info("采用借号方案处理时钟回拨");
        
        // 使用上一个时间戳和借用的序列号
        if (borrowedTimestamp != lastTimestamp) {
            borrowedTimestamp = lastTimestamp;
            borrowedSequence.set(MAX_SEQUENCE);
        }
        
        long borrowed = borrowedSequence.decrementAndGet();
        if (borrowed < 0) {
            // 借号用完，抛出异常
            throw new RuntimeException("借号序列号已用完，无法生成ID");
        }
        
        sequence = borrowed;
        logger.debug("使用借号序列号: {}", sequence);
    }
    
    /**
     * 等待下一毫秒
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
    
    /**
     * 自动生成机器ID
     */
    private long generateMachineId() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            int hash = hostName.hashCode();
            return Math.abs(hash) % (MAX_MACHINE_NUM + 1);
        } catch (UnknownHostException e) {
            logger.warn("无法获取主机名，使用随机机器ID");
            return ThreadLocalRandom.current().nextLong(0, MAX_MACHINE_NUM + 1);
        }
    }
    
    /**
     * 上报时钟回拨告警
     */
    private void reportClockBackwardAlert(long backwardTime) {
        // 这里可以集成具体的监控系统，如Prometheus、Micrometer等
        logger.error("【监控告警】时钟回拨检测 - 回拨时间: {} ms, 数据中心: {}, 机器: {}", 
                backwardTime, datacenterId, machineId);
        
        // 可以在这里添加具体的告警逻辑
        // 例如：发送邮件、短信、钉钉消息等
        // alertService.sendClockBackwardAlert(backwardTime, datacenterId, machineId);
    }
    
    /**
     * 获取监控统计信息
     */
    public MonitorInfo getMonitorInfo() {
        return new MonitorInfo(
                totalGenerated.get(),
                clockBackwardCount.get(),
                waitStrategyCount.get(),
                borrowStrategyCount.get(),
                clockBackwardDetected,
                datacenterId,
                machineId
        );
    }
    
    /**
     * 监控信息DTO
     */
    public static class MonitorInfo {
        private final long totalGenerated;
        private final long clockBackwardCount;
        private final long waitStrategyCount;
        private final long borrowStrategyCount;
        private final boolean clockBackwardDetected;
        private final long datacenterId;
        private final long machineId;
        
        public MonitorInfo(long totalGenerated, long clockBackwardCount, long waitStrategyCount, 
                          long borrowStrategyCount, boolean clockBackwardDetected, 
                          long datacenterId, long machineId) {
            this.totalGenerated = totalGenerated;
            this.clockBackwardCount = clockBackwardCount;
            this.waitStrategyCount = waitStrategyCount;
            this.borrowStrategyCount = borrowStrategyCount;
            this.clockBackwardDetected = clockBackwardDetected;
            this.datacenterId = datacenterId;
            this.machineId = machineId;
        }
        
        // Getters
        public long getTotalGenerated() { return totalGenerated; }
        public long getClockBackwardCount() { return clockBackwardCount; }
        public long getWaitStrategyCount() { return waitStrategyCount; }
        public long getBorrowStrategyCount() { return borrowStrategyCount; }
        public boolean isClockBackwardDetected() { return clockBackwardDetected; }
        public long getDatacenterId() { return datacenterId; }
        public long getMachineId() { return machineId; }
    }
} 