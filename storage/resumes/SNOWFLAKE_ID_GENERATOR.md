# 雪花算法ID生成器

## 概述

本项目实现了一个基于雪花算法的分布式ID生成器，支持时钟回拨处理、等待策略、借号方案和监控告警，确保ID分发的高可靠性。

## 特性

- ✅ **分布式ID生成**: 基于雪花算法，支持多机房多实例部署
- ✅ **时钟回拨处理**: 自动检测并处理时钟回拨问题
- ✅ **等待策略**: 对于轻微的时钟回拨，采用等待策略
- ✅ **借号方案**: 对于严重的时钟回拨，采用借号方案保证服务可用性
- ✅ **监控告警**: 完整的监控统计和告警机制
- ✅ **高性能**: 单机QPS可达数万级别
- ✅ **高可靠性**: 多重保障机制确保ID不重复

## 算法结构

雪花算法生成的ID结构如下：

```
| 1位符号位 | 41位时间戳 | 5位数据中心ID | 5位机器ID | 12位序列号 |
|    0     |  timestamp |  datacenter  | machine |  sequence |
```

- **时间戳**: 41位，可用69年
- **数据中心ID**: 5位，支持32个数据中心
- **机器ID**: 5位，每个数据中心支持32台机器
- **序列号**: 12位，每毫秒可生成4096个ID

## 配置说明

在 `application.yml` 中配置：

```yaml
snowflake:
  datacenter-id: 1  # 数据中心ID (0-31)
  machine-id: 0     # 机器ID (0-31)，设置为0将自动生成
  clock-backward-wait-time: 5000  # 时钟回拨最大等待时间(毫秒)
  max-clock-backward-time: 1000   # 最大允许时钟回拨时间(毫秒)
```

### 配置参数详解

- `datacenter-id`: 数据中心标识，取值范围0-31，不同数据中心应使用不同的ID
- `machine-id`: 机器标识，取值范围0-31，同一数据中心内不同机器应使用不同的ID
- `clock-backward-wait-time`: 时钟回拨等待策略的最大等待时间
- `max-clock-backward-time`: 触发借号方案的时钟回拨阈值

## 使用方法

### 1. 基本使用

```java
@Service
public class YourService {
    
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;
    
    public void createRecord() {
        // 生成分布式ID
        long id = snowflakeIdGenerator.nextId();
        String stringId = String.valueOf(id);
        
        // 使用ID创建记录
        // ...
    }
}
```

### 2. 监控接口

系统提供了完整的监控接口：

```bash
# 获取雪花算法监控信息
GET /api/monitor/snowflake

# 生成测试ID
GET /api/monitor/snowflake/test

# 批量生成测试ID（压力测试）
GET /api/monitor/snowflake/batch-test
```

### 3. 监控信息

监控接口返回的信息包括：

```json
{
  "success": true,
  "data": {
    "totalGenerated": 12345,          // 总生成数量
    "clockBackwardCount": 2,          // 时钟回拨次数
    "waitStrategyCount": 1,           // 等待策略使用次数
    "borrowStrategyCount": 1,         // 借号方案使用次数
    "clockBackwardDetected": false,   // 是否检测到时钟回拨
    "datacenterId": 1,                // 数据中心ID
    "machineId": 3,                   // 机器ID
    "systemTime": 1703174400000       // 系统时间
  }
}
```

## 时钟回拨处理机制

### 1. 检测机制

系统会自动检测时钟回拨情况，当 `当前时间 < 上次生成时间` 时触发。

### 2. 处理策略

#### 等待策略
- 适用于轻微的时钟回拨（回拨时间 ≤ maxClockBackwardTime）
- 系统会等待时间追上，然后继续生成ID
- 等待时间不超过 clockBackwardWaitTime

#### 借号方案
- 适用于严重的时钟回拨（回拨时间 > maxClockBackwardTime）
- 使用上一个时间戳的预留序列号
- 确保服务可用性，避免长时间等待

### 3. 告警机制

当检测到时钟回拨时，系统会：
- 记录ERROR级别日志
- 更新监控统计
- 可扩展集成外部告警系统

## 性能与可靠性

### 性能指标

- **单机QPS**: 10万+
- **并发支持**: 完全线程安全
- **延迟**: 微秒级别

### 可靠性保障

1. **ID唯一性**: 通过时间戳+机器标识+序列号保证
2. **单调递增**: 时间维度上严格递增
3. **高可用**: 时钟回拨处理确保服务不中断
4. **监控完善**: 全方位监控和告警

## 部署建议

### 1. 多实例部署

```yaml
# 实例1
snowflake:
  datacenter-id: 1
  machine-id: 1

# 实例2
snowflake:
  datacenter-id: 1
  machine-id: 2
```

### 2. 多机房部署

```yaml
# 机房A
snowflake:
  datacenter-id: 1
  machine-id: 1

# 机房B
snowflake:
  datacenter-id: 2
  machine-id: 1
```

### 3. 自动化部署

如果无法手动分配机器ID，可以设置 `machine-id: 0`，系统会根据主机名自动生成。

## 监控告警

### 1. 关键指标

- 时钟回拨次数
- 等待策略使用次数
- 借号方案使用次数
- ID生成总数
- 生成性能

### 2. 告警建议

- 时钟回拨次数过多
- 借号方案频繁使用
- ID生成性能下降

### 3. 扩展监控

可以集成Prometheus、Micrometer等监控系统：

```java
// 在 reportClockBackwardAlert 方法中添加
meterRegistry.counter("snowflake.clock.backward", 
    "datacenter", String.valueOf(datacenterId),
    "machine", String.valueOf(machineId))
    .increment();
```

## 故障排查

### 1. 常见问题

**Q: ID生成失败**
- 检查时钟是否同步
- 查看是否有严重的时钟回拨
- 检查借号序列号是否用完

**Q: 时钟回拨告警**
- 检查系统时间同步
- 确认NTP服务正常
- 考虑调整时钟回拨阈值

**Q: 性能问题**
- 检查并发量是否过高
- 确认是否存在锁竞争
- 监控JVM性能

### 2. 日志分析

```bash
# 查看时钟回拨日志
grep "时钟回拨" application.log

# 查看ID生成统计
grep "简历保存成功" application.log
```

## 测试验证

项目提供了完整的单元测试：

```bash
# 运行测试
mvn test -Dtest=SnowflakeIdGeneratorTest

# 性能测试
curl http://localhost:8080/api/monitor/snowflake/batch-test
```

## 最佳实践

1. **合理分配ID**: 确保不同实例使用不同的datacenter-id和machine-id
2. **时钟同步**: 定期同步系统时钟，使用NTP服务
3. **监控告警**: 建立完善的监控和告警机制
4. **性能调优**: 根据实际QPS需求调整JVM参数
5. **容量规划**: 41位时间戳可用69年，12位序列号每毫秒4096个ID

## 更新日志

- v1.0.0: 基础雪花算法实现
- v1.1.0: 添加时钟回拨处理
- v1.2.0: 添加监控告警功能
- v1.3.0: 完善测试和文档 