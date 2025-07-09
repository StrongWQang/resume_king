package com.example.resumebuilder.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SnowflakeIdGeneratorTest {
    
    private SnowflakeIdGenerator snowflakeIdGenerator;
    
    @BeforeEach
    void setUp() {
        snowflakeIdGenerator = new SnowflakeIdGenerator();
        // 设置测试配置
        ReflectionTestUtils.setField(snowflakeIdGenerator, "datacenterId", 1L);
        ReflectionTestUtils.setField(snowflakeIdGenerator, "machineId", 1L);
        ReflectionTestUtils.setField(snowflakeIdGenerator, "clockBackwardWaitTime", 5000L);
        ReflectionTestUtils.setField(snowflakeIdGenerator, "maxClockBackwardTime", 1000L);
        
        // 初始化
        snowflakeIdGenerator.init();
    }
    
    @Test
    void testNextIdGeneration() {
        // 测试基本ID生成
        long id1 = snowflakeIdGenerator.nextId();
        long id2 = snowflakeIdGenerator.nextId();
        
        assertNotEquals(id1, id2);
        assertTrue(id1 > 0);
        assertTrue(id2 > 0);
        assertTrue(id2 > id1); // 后生成的ID应该更大
    }
    
    @Test
    void testUniqueIds() {
        // 测试ID唯一性
        Set<Long> ids = new HashSet<>();
        int count = 10000;
        
        for (int i = 0; i < count; i++) {
            long id = snowflakeIdGenerator.nextId();
            assertTrue(ids.add(id), "发现重复ID: " + id);
        }
        
        assertEquals(count, ids.size());
    }
    
    @Test
    void testConcurrentIdGeneration() throws InterruptedException {
        // 测试并发ID生成
        int threadCount = 10;
        int idsPerThread = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<Long> allIds = new HashSet<>();
        
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    Set<Long> threadIds = new HashSet<>();
                    for (int j = 0; j < idsPerThread; j++) {
                        long id = snowflakeIdGenerator.nextId();
                        threadIds.add(id);
                    }
                    
                    synchronized (allIds) {
                        allIds.addAll(threadIds);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executorService.shutdown();
        
        // 验证所有ID都是唯一的
        assertEquals(threadCount * idsPerThread, allIds.size());
    }
    
    @Test
    void testMonitorInfo() {
        // 测试监控信息
        SnowflakeIdGenerator.MonitorInfo initialInfo = snowflakeIdGenerator.getMonitorInfo();
        long initialGenerated = initialInfo.getTotalGenerated();
        
        // 生成一些ID
        for (int i = 0; i < 10; i++) {
            snowflakeIdGenerator.nextId();
        }
        
        SnowflakeIdGenerator.MonitorInfo currentInfo = snowflakeIdGenerator.getMonitorInfo();
        assertEquals(initialGenerated + 10, currentInfo.getTotalGenerated());
        assertEquals(1L, currentInfo.getDatacenterId());
        assertEquals(1L, currentInfo.getMachineId());
    }
    
    @Test
    void testIdStructure() {
        // 测试ID结构
        long id = snowflakeIdGenerator.nextId();
        
        // 验证ID为正数
        assertTrue(id > 0);
        
        // 验证ID长度合理（雪花算法生成的ID通常是18-19位）
        String idStr = String.valueOf(id);
        assertTrue(idStr.length() >= 15 && idStr.length() <= 20);
    }
    
    @Test
    void testIdIncreasing() {
        // 测试ID递增性
        long previousId = snowflakeIdGenerator.nextId();
        
        for (int i = 0; i < 100; i++) {
            long currentId = snowflakeIdGenerator.nextId();
            assertTrue(currentId > previousId, 
                String.format("ID应该递增，但 %d <= %d", currentId, previousId));
            previousId = currentId;
        }
    }
    
    @Test
    void testBatchGeneration() {
        // 测试批量生成性能
        int batchSize = 10000;
        long startTime = System.currentTimeMillis();
        
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < batchSize; i++) {
            ids.add(snowflakeIdGenerator.nextId());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证唯一性
        assertEquals(batchSize, ids.size());
        
        // 验证性能（应该在合理时间内完成）
        assertTrue(duration < 10000, "批量生成耗时过长: " + duration + "ms");
        
        // 打印性能统计
        System.out.println("批量生成 " + batchSize + " 个ID耗时: " + duration + "ms");
        System.out.println("平均每个ID耗时: " + (duration / (double) batchSize) + "ms");
    }
} 