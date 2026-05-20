# 食品安全数据存储方案

## 1. 论文概述

**参考**: 《食品安全质量检测学报》第 17 卷 - 基于智能区块链的食品溯源数据存储方案

---

## 2. 核心设计理念

### 2.1 链上链下分离存储

**问题**: 传统区块链要求每个节点存储完整数据副本
- 存储成本高
- 可扩展性差
- 随区块高度增加存储压力剧增

**解决方案**: 借鉴 MetaDisk 等分布式文件存储系统设计

```
┌─────────────────────────────────────────────────────┐
│               数据存储架构                            │
├─────────────────────────────────────────────────────┤
│  链上 (On-Chain)        │  链下 (Off-Chain)          │
│  ─────────────          │  ──────────────            │
│  · 数据摘要 (哈希)        │  · 原始数据 (密文)          │
│  · 时间戳               │  · 分布式数据库/云存储       │
│  · 交易记录             │  · 参与方共同维护           │
│  · 元数据 (索引)         │                           │
│                         │                           │
│  优势:                  │  优势:                     │
│  - 确保可追溯性          │  - 降低节点存储负担         │
│  - 确保完整性           │  - 高效管理大规模数据       │
│  - 不可篡改             │  - 良好的可扩展性          │
└─────────────────────────┴───────────────────────────┘
```

### 2.2 五层核心架构

| 组件 | 职责 | 说明 |
|------|------|------|
| 1. 联盟区块链 | 存储摘要 + 交易记录 + 智能合约执行 | 基础设施核心 |
| 2. 食品供应链参与方 | 节点参与数据共享与交互 | 生产商/加工商/物流商/零售商 |
| 3. 食品数据存储服务器 | 密文存储原始数据 | 分布式数据库或云存储 |
| 4. 服务接口层 | API 接口 | 智能合约调用和数据存储 |
| 5. 智能合约 | 身份验证 + 业务逻辑 | 数据共享/溯源 |

---

## 3. 技术创新：Bloom Filter 扩展区块头

### 3.1 传统区块结构

```
┌──────────────────────────────────────┐
│           传统区块头                   │
├──────────────────────────────────────┤
│  - 版本号                             │
│  - 前一个区块哈希                      │
│  - Merkle 根                          │
│  - 时间戳                             │
│  - 难度目标                           │
│  - 随机数                             │
└──────────────────────────────────────┘
```

**问题**: 数据检索需要遍历区块体，效率低

### 3.2 优化区块结构 (基于 Bloom Filter)

```
┌──────────────────────────────────────┐
│         优化区块头 (扩展)              │
├──────────────────────────────────────┤
│  - 版本号                             │
│  - 前一个区块哈希                      │
│  - Merkle 根                          │
│  - 时间戳                             │
│  - 难度目标                           │
│  - 随机数                             │
│  - Bloom Filter  (新增)              │
│  - 元数据索引 (新增)                   │
└──────────────────────────────────────┘
```

**Bloom Filter 优势**:
- 快速判断某个食品数据是否在区块链中
- 减少不必要的磁盘 I/O
- 提高检索速度和准确性
- 空间效率高

### 3.3 Bloom Filter 工作原理

```
食品溯源码 → 哈希函数 1 → 位数组位置 1
           → 哈希函数 2 → 位数组位置 2
           → 哈希函数 3 → 位数组位置 3

查询时:
- 如果所有哈希位置都是 1 → 可能存在 (需要进一步验证)
- 如果任意哈希位置是 0 → 一定不存在
```

**特点**:
- 允许少量假阳性 (False Positive)
- 无假阴性 (False Negative)
- 适合食品溯源快速查询场景

---

## 4. 数据流程

### 4.1 食品数据共享流程

```
步骤 1: 加入联盟链
   ┌──────────────────────────────────────────┐
   │ 参与方 → 智能合约申请 → 身份验证 → 成为节点 │
   └──────────────────────────────────────────┘
              ↓
步骤 2: 数据发布
   ┌──────────────────────────────────────────┐
   │ 生产商完成食品生产/处理                    │
   │   ↓                                      │
   │ 上传食品描述信息 + 访问权限到区块链         │
   │   ↓                                      │
   │ 原始数据加密 → 存储到分布式数据库          │
   └──────────────────────────────────────────┘
              ↓
步骤 3: 数据访问
   ┌──────────────────────────────────────────┐
   │ 参与方通过 API 访问区块链数据               │
   │   ↓                                      │
   │ 智能合约验证访问权限                       │
   │   ↓                                      │
   │ 获取链上元数据 → 定位链下存储              │
   │   ↓                                      │
   │ 解密原始数据 → 完成数据共享               │
   └──────────────────────────────────────────┘
```

### 4.2 数据存储模式

```
┌─────────────────────────────────────────────────────────┐
│              链上元数据 + 链下分布式存储                   │
├──────────────────────┬──────────────────────────────────┤
│     链上 (区块链)     │      链下 (分布式存储)            │
├──────────────────────┼──────────────────────────────────┤
│  食品 ID (唯一标识)    │  原始数据 (加密)                  │
│  数据哈希 (SHA-256)  │  - 生产记录                      │
│  时间戳              │  - 检测报告                      │
│  数据所有者          │  - 物流信息                      │
│  访问控制策略        │  - 存储条件                      │
│  Bloom Filter       │  - 销售记录                      │
│  存储位置指针        │  - 图片/视频                     │
└──────────────────────┴──────────────────────────────────┘
```

---

## 5. 针对本项目的简化方案

### 5.1 实际情况分析

**论文方案**: 联盟区块链 + 分布式数据库/云存储 (多个节点)
**项目现状**: 最多使用两个数据库

**约束条件**:
- 单节点部署或主从部署
- 资源有限
- 不需要应对超大规模数据

### 5.2 简化存储架构

```
┌─────────────────────────────────────────────────────────┐
│                  简化存储架构                              │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────┐      ┌─────────────────────┐      │
│  │   MySQL 数据库   │      │   Redis 缓存/存储    │      │
│  │                 │      │                     │      │
│  │ · 区块链日志表   │      │ · 热点数据缓存       │      │
│  │ · 元数据索引表   │      │ · Bloom Filter      │      │
│  │ · 智能合约状态   │      │ · 会话/令牌存储      │      │
│  │ · 业务数据表     │      │ · 队列/消息          │      │
│  └─────────────────┘      └─────────────────────┘      │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │              应用层 (Spring Boot)                │   │
│  │  · Agent 多代理系统                               │   │
│  │  · PBFT 共识模块                                  │   │
│  │  · 智能合约引擎                                   │   │
│  │  · 数据加密/解密                                 │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 5.3 数据库表设计

#### MySQL 表结构

**1. blockchain_log (区块链日志表)**
```sql
CREATE TABLE blockchain_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chain_type VARCHAR(30) NOT NULL,      -- MATERIAL/BATCH/TRANSPORT/SALES
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    previous_hash VARCHAR(128),
    current_hash VARCHAR(128) NOT NULL,
    data_snapshot TEXT,                    -- 数据快照/摘要
    signature VARCHAR(512) NOT NULL,
    timestamp DATETIME NOT NULL,
    operator_id BIGINT,
    ref_master_chain_hash VARCHAR(128),
    batch_id BIGINT,
    bloom_filter BLOB,                     -- Bloom Filter 数据
    metadata_index JSON,                   -- 元数据索引
    INDEX idx_chain_type (chain_type),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_timestamp (timestamp)
);
```

**2. offchain_storage_reference (链下存储引用表)**
```sql
CREATE TABLE offchain_storage_reference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    food_id VARCHAR(100) UNIQUE NOT NULL,  -- 食品唯一标识
    data_hash VARCHAR(64) NOT NULL,        -- 原始数据哈希
    storage_type VARCHAR(20) NOT NULL,     -- LOCAL/REDIS/OSS
    storage_key VARCHAR(200) NOT NULL,     -- 存储位置/键
    encryption_method VARCHAR(50),         -- 加密算法
    owner_agent_id BIGINT NOT NULL,        -- 数据所有者
    access_policy JSON,                    -- 访问控制策略
    created_at DATETIME NOT NULL,
    expires_at DATETIME,
    INDEX idx_food_id (food_id),
    INDEX idx_storage (storage_type, storage_key)
);
```

**3. smart_contract_state (智能合约状态表)**
```sql
CREATE TABLE smart_contract_state (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id VARCHAR(50) UNIQUE NOT NULL,
    contract_type VARCHAR(50) NOT NULL,    -- PERMISSION/TRANSACTION/DATA/STATE
    state_key VARCHAR(200) NOT NULL,
    state_value TEXT,
    version BIGINT NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE INDEX idx_contract_state (contract_id, state_key)
);
```

**4. agent_identity (Agent 身份表)**
```sql
CREATE TABLE agent_identity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    agent_id VARCHAR(50) UNIQUE NOT NULL,
    agent_type VARCHAR(30) NOT NULL,       -- PRODUCTION/CIRCULATION/SALES/CA
    certificate_serial VARCHAR(100),       -- 证书序列号
    public_key TEXT,                       -- 公钥
    credit_score BIGINT DEFAULT 100,       -- 信誉分
    status VARCHAR(20) DEFAULT 'ACTIVE',   -- ACTIVE/SUSPENDED/REVOKED
    registered_at DATETIME NOT NULL,
    last_active_at DATETIME,
    metadata JSON,
    INDEX idx_agent_type (agent_type),
    INDEX idx_status (status)
);
```

#### Redis 数据结构

```
# Bloom Filter
BF.RESERVE food_bloom 0.01 1000000
BF.ADD food_bloom "FOOD-2026-001"
BF.EXISTS food_bloom "FOOD-2026-001"  # 快速查询

# 热点数据缓存
SET food:FOOD-2026-001:data "{encrypted_data}"
EXPIRE food:FOOD-2026-001:data 3600

# 会话管理
SET session:agent:P-001 "{session_info}"
EXPIRE session:agent:P-001 1800

# 消息队列
LPUSH consensus:queue "{pbft_message}"
RPOP consensus:queue
```

---

## 6. 数据加密方案

### 6.1 对称加密 + 非对称加密混合

```
原始数据加密流程:
┌──────────────────────────────────────────┐
│  1. 生成随机 AES-256 密钥                 │
│  2. 使用 AES 密钥加密原始数据              │
│  3. 使用接收方公钥加密 AES 密钥            │
│  4. 存储：加密数据 + 加密的 AES 密钥        │
└──────────────────────────────────────────┘

数据解密流程:
┌──────────────────────────────────────────┐
│  1. 使用接收方私钥解密 AES 密钥            │
│  2. 使用 AES 密钥解密数据                 │
│  3. 验证数据哈希完整性                   │
└──────────────────────────────────────────┘
```

### 6.2 加密算法选择

| 用途 | 算法 | 密钥长度 |
|------|------|----------|
| 数据加密 | AES | 256 位 |
| 密钥交换 | RSA | 2048 位 |
| 数据哈希 | SHA-256 | 256 位 |
| 数字签名 | RSA-PSS | 2048 位 |

---

## 7. 访问控制策略

### 7.1 基于角色的访问控制 (RBAC)

```json
{
  "accessPolicy": {
    "foodId": "FOOD-2026-001",
    "owner": "P-001",
    "rules": [
      {
        "role": "PRODUCER",
        "agentTypes": ["PRODUCTION"],
        "permissions": ["READ", "WRITE", "DELETE"]
      },
      {
        "role": "CIRCULATOR",
        "agentTypes": ["CIRCULATION"],
        "permissions": ["READ", "WRITE"]
      },
      {
        "role": "SELLER",
        "agentTypes": ["SALES"],
        "permissions": ["READ"]
      },
      {
        "role": "REGULATOR",
        "agentTypes": ["REGULATORY"],
        "permissions": ["READ", "AUDIT"]
      },
      {
        "role": "CONSUMER",
        "agentTypes": ["CONSUMER"],
        "permissions": ["READ_PUBLIC"]
      }
    ]
  }
}
```

### 7.2 智能合约验证流程

```
访问请求 → 智能合约
    ↓
验证 Agent 身份 (CA-Agent)
    ↓
检查信誉分 (是否 >= 阈值)
    ↓
验证访问策略 (RBAC)
    ↓
签名验证
    ↓
允许/拒绝访问
```

---

## 8. Bloom Filter 实现

### 8.1 Java 实现示例

```java
import java.util.BitSet;
import java.util.Base64;

public class FoodBloomFilter {
    private final BitSet bitSet;
    private final int hashCount;
    private final int size;
    
    public FoodBloomFilter(int expectedElements, double falsePositiveRate) {
        this.size = (int) (-expectedElements * Math.log(falsePositiveRate) 
                          / (Math.log(2) * Math.log(2)));
        this.hashCount = (int) (size / expectedElements * Math.log(2));
        this.bitSet = new BitSet(size);
    }
    
    public void add(String foodId) {
        for (int i = 0; i < hashCount; i++) {
            int hash = hash(foodId, i);
            bitSet.set(hash % size);
        }
    }
    
    public boolean mightContain(String foodId) {
        for (int i = 0; i < hashCount; i++) {
            int hash = hash(foodId, i);
            if (!bitSet.get(hash % size)) {
                return false;
            }
        }
        return true;
    }
    
    private int hash(String foodId, int seed) {
        return Objects.hash(seed, foodId);
    }
    
    public byte[] toBytes() {
        return bitSet.toByteArray();
    }
    
    public static FoodBloomFilter fromBytes(byte[] bytes, int expectedElements) {
        FoodBloomFilter filter = new FoodBloomFilter(expectedElements, 0.01);
        filter.bitSet.or(BitSet.valueOf(bytes));
        return filter;
    }
}
```

### 8.2 集成到区块头

```java
public class OptimizedBlockHeader {
    private String version;
    private String previousHash;
    private String merkleRoot;
    private LocalDateTime timestamp;
    private Long nonce;
    private byte[] bloomFilter;      // 新增
    private JsonNode metadataIndex;  // 新增
    
    // 快速查询食品数据是否存在
    public boolean mightContainFoodData(String foodId) {
        FoodBloomFilter filter = FoodBloomFilter.fromBytes(bloomFilter, 10000);
        return filter.mightContain(foodId);
    }
}
```

---

## 9. 性能优化建议

### 9.1 数据库优化

```sql
-- 1. 分区表 (按时间)
ALTER TABLE blockchain_log 
PARTITION BY RANGE (YEAR(timestamp)) (
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027)
);

-- 2. 覆盖索引
CREATE INDEX idx_blockchain_query 
ON blockchain_log(chain_type, entity_type, entity_id, timestamp);

-- 3. 读写分离
-- 主库：写操作
-- 从库：查询操作
```

### 9.2 缓存策略

```java
@CacheConfig(cacheNames = "foodData")
@Service
public class FoodDataService {
    
    @Cacheable(key = "#foodId", unless = "#result == null")
    public EncryptedFoodData getFoodData(String foodId) {
        // 先查 Redis 缓存
        // 缓存未命中则查数据库
    }
    
    @CacheEvict(key = "#foodId")
    public void updateFoodData(String foodId, FoodData data) {
        // 更新后清除缓存
    }
}
```

### 9.3 批量操作优化

```java
// 批量上链 (减少交易次数)
public void batchAppendToChain(List<FoodData> dataList) {
    String batchHash = calculateBatchHash(dataList);
    
    // 单次交易记录多条数据
    blockchainService.appendBatchChainBlock(
        batchHash,
        "FOOD_BATCH",
        dataList.size(),
        "BATCH_UPLOAD",
        toJson(dataList),
        operatorId
    );
}
```

---

## 10. 实施路线图

### 阶段 1: 基础架构 (已完成)
- ✅ 多 Agent 系统
- ✅ PBFT 共识
- ✅ 智能合约层

### 阶段 2: 存储优化 (进行中)
- 🔄 Bloom Filter 集成
- 🔄 链上链下分离
- 🔄 数据加密模块

### 阶段 3: 性能提升
- ⏳ Redis 缓存层
- ⏳ 数据库分区/索引优化
- ⏳ 批量操作优化

### 阶段 4: 安全加固
- ⏳ 完善的 RBAC 权限控制
- ⏳ 审计日志
- ⏳ 数据备份/恢复

---

## 11. 总结

### 核心设计原则

1. **链上最小化**: 仅存储摘要和索引，降低存储压力
2. **链下高效化**: 原始数据加密存储，支持大规模扩展
3. **快速检索**: Bloom Filter 加速查询
4. **安全保障**: 混合加密 + RBAC 访问控制
5. **实际可行**: 适配两数据库架构 (MySQL + Redis)

### 与论文的差异

| 论文方案 | 项目简化方案 |
|----------|--------------|
| 联盟区块链多节点 | 单节点/主从 |
| 分布式数据库/云存储 | MySQL + Redis |
| P2P 网络连接 | 应用层 Agent 协作 |
| 完整 Bloom Filter 区块头 | 简化 Bloom Filter 索引 |
| 完整智能合约引擎 | Spring Boot 合约模拟 |

### 预期效果

- 存储成本降低 80%+ (链上只存摘要)
- 查询速度提升 10 倍+ (Bloom Filter 过滤)
- 支持百万级食品数据追溯
- 满足项目实际需求 (双数据库架构)

---

**文档生成日期**: 2026-05-20
**关联分支**: `260520-feat-blockchain-agent-architecture`
**参考论文**: 
1. 《基于区块链的食品溯源多 Agent 系统架构的研究》
2. 《食品安全质量检测学报》第 17 卷 - 数据存储方案
