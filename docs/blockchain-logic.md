# 区块链防篡改逻辑

## 整体架构

双链结构：**物料链**（全局单链）+ **批次链**（每批次一条独立链），通过 `ref_master_chain_hash` 交叉链接。

```
物料链（全局）:
  [MATERIAL:1] ← [MATERIAL_PURCHASE:1] ← [MATERIAL:2] ← ...
                                                    ↓ ref_master_chain_hash
批次链（每批次独立）:
  [PRODUCTION_BATCH] ← [STORAGE] ← [INSPECTION] ← [TRANSPORT_SALE]
```

---

## 一、核心实体

### BlockchainLog → 表 `blockchain_log`

| 字段 | 说明 |
|---|---|
| `id` | 主键自增 |
| `chain_type` | `MATERIAL` 或 `BATCH` |
| `batch_id` | 批次链的批次ID，物料链为null |
| `entity_type` | 实体类型：MATERIAL / MATERIAL_PURCHASE / PRODUCTION_BATCH / STORAGE / INSPECTION / TRANSPORT_SALE |
| `entity_id` | 对应实体记录的ID |
| `action` | CREATE / UPDATE / DEACTIVATE 等 |
| `previous_hash` | 前一个区块的 SHA-256 哈希 |
| `current_hash` | 本区块的 SHA-256 哈希 |
| `data_snapshot` | 实体状态的 JSON 快照 |
| `signature` | current_hash 的 RSA 签名（Base64） |
| `timestamp` | 区块创建时间 |
| `operator_id` | 操作用户ID |
| `ref_master_chain_hash` | 批次链专用：创建时全局物料链最新区块的哈希值，用于跨链链接 |

### BlockchainAnchor → 表 `blockchain_anchor`

每日快照：记录每条链当天结束时的最新哈希值，用于外部验证。

---

## 二、哈希值与签名机制

### SHA-256 当前哈希值计算

**计算方法** (`BlockchainService.calculateHash()`):

```java
public String calculateHash(String entityType, Long entityId, String action,
                             String previousHash, String dataSnapshot, LocalDateTime timestamp,
                             Long batchId, Long operatorId, String refMasterChainHash) {
    String input = entityType + "|" + entityId + "|" + action + "|"
            + (previousHash != null ? previousHash : "") + "|"
            + (dataSnapshot != null ? dataSnapshot : "") + "|"
            + timestamp + "|"
            + (batchId != null ? batchId : "") + "|"
            + (operatorId != null ? operatorId : "") + "|"
            + (refMasterChainHash != null ? refMasterChainHash : "");
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
    return bytesToHex(hash);
}
```

**输入字段**（9 个，用 `|` 拼接）：

| # | 字段 | 物料链 | 批次链 |
|---|---|---|---|
| 1 | `entityType` | MATERIAL / MATERIAL_PURCHASE | PRODUCTION_BATCH / STORAGE / INSPECTION / TRANSPORT_SALE |
| 2 | `entityId` | 对应记录的 ID | 对应记录的 ID |
| 3 | `action` | CREATE / UPDATE / ACTIVATE / DEACTIVATE | CREATE |
| 4 | `previousHash` | 前一个物料区块哈希 | 该批次前一个区块哈希 |
| 5 | `dataSnapshot` | 物料/采购的 JSON 快照 | 批次各环节的 JSON 快照 |
| 6 | `timestamp` | 区块创建时间 | 区块创建时间 |
| 7 | `batchId` | null | 该批次的 ID |
| 8 | `operatorId` | 操作人（当前始终为 null） | 操作人（当前始终为 null） |
| 9 | `refMasterChainHash` | null | 创建时全局物料链最新区块哈希 |

**输入示例**（一条 Storage 区块）：

字段 | 值
---|---
`entityType` | `"STORAGE"`
`entityId` | `21`
`action` | `"CREATE"`
`previousHash` | `"a1b2c3d4..."`（前一个区块的 current_hash）
`dataSnapshot` | `{"id":21,"batchId":5,"storageTime":"2026-05-10T14:30:00","quantity":100.0,"unit":"kg","warehouseLocation":"A区3号"}`
`timestamp` | `2026-05-10T14:30:05`

最终输入字符串:
```
STORAGE|21|CREATE|a1b2c3d4...|{"id":21,...}|2026-05-10T14:30:05|5|null|null
```

→ SHA-256 → `7e8f9a0b...` (64位十六进制)

**字节转十六进制** (`bytesToHex`):

```java
private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
        sb.append(String.format("%02x", b));  // 每个字节2位十六进制，小写
    }
    return sb.toString();
}
```

SHA-256 输出 32 字节 → `32 × 2 = 64` 位小写十六进制字符串。

### previous_hash 的获取

追加区块时查询数据库获取前一个区块的 `current_hash`：

```java
// 物料链（全局）
String previousHash = blockchainLogRepo
    .findTopByChainTypeOrderByTimestampDesc("MATERIAL")
    .map(BlockchainLog::getCurrentHash)
    .orElse(null);  // 首个区块取 null

// 批次链（按 batch_id）
String previousHash = blockchainLogRepo
    .findTopByChainTypeAndBatchIdOrderByTimestampDesc("BATCH", batchId)
    .map(BlockchainLog::getCurrentHash)
    .orElse(null);  // 该批次首个区块取 null
```

首个区块 `previous_hash = null`，拼接时替代为空字符串。

### 链式链接示例

```
区块1（首个）：
  input = "MATERIAL|1|CREATE||{"id":1,"name":"生牛乳"}|2026-05-10T10:00:00"
  previous_hash = (null → "")
  current_hash  = SHA-256(input) = "a1b2..."

区块2：
  input = "MATERIAL_PURCHASE|1|CREATE|a1b2...|{"id":1,...}|2026-05-10T10:05:00"
  previous_hash = "a1b2..."  ← 区块1的 current_hash
  current_hash  = SHA-256(input) = "c3d4..."

区块3：
  input = "MATERIAL|2|UPDATE|c3d4...|{"id":2,"isActive":false}|2026-05-10T11:00:00"
  previous_hash = "c3d4..."  ← 区块2的 current_hash
  current_hash  = SHA-256(input) = "e5f6..."
```

每个区块的 `previous_hash` 就是前一个区块的 `current_hash`，形成单向链表。修改任意区块的数据 → 该区块 `current_hash` 改变 → 后续所有区块的 `previous_hash` 不匹配 → 链断裂。

### 完整上链流程

`appendMaterialChainBlock()` / `appendBatchChainBlock()`:

```
1. 查前一个区块 → 取 previous_hash
2. 查当前物料链最新哈希 → ref_master_chain_hash（仅批次链）
3. 取当前时间 LocalDateTime.now() → timestamp
4. 计算 SHA-256: calculateHash(entityType, entityId, action, previousHash, dataSnapshot, timestamp, batchId, operatorId, refMasterChainHash) → current_hash
5. 对 current_hash 做 RSA 签名 → signature
6. 组装 BlockchainLog 实体
7. save 到 blockchain_log 表
```

### RSA-2048 签名

- 算法：`SHA256withRSA`
- 密钥：2048位 RSA 密钥对
- 来源：`RsaKeyConfig.java` — 优先从 `blockchain.rsa.private-key-path` / `blockchain.rsa.public-key-path` 配置加载 PEM 文件，未配置则运行时自动生成
- 签名内容：`current_hash` 的原始字节
- 输出：Base64 编码字符串
- 验签：公钥通过 `GET /api/blockchain/public-key` 对外公开，供第三方验证

---

## 三、链操作

### 追加区块

`BlockchainService.appendMaterialChainBlock()` / `appendBatchChainBlock()`

1. 查询前一个区块（按 `timestamp DESC` 取最新）
2. 获取前一个区块的 `current_hash` 作为 `previous_hash`
3. 拼接哈希入参，计算 SHA-256
4. 用私钥对 `current_hash` 签名
5. 如果是批次链，查询当前物料链最新区块哈希作为 `ref_master_chain_hash`
6. 保存 `BlockchainLog`

### 链验证

`BlockchainService.verifyChain()`

遍历链上所有区块（按 `timestamp ASC`），对每个区块做三项检查：

1. **前向链接**：`block.previousHash == 前一个区块.currentHash` — 检测链是否断裂
2. **哈希完整性**：用相同入参重新计算 SHA-256，比对 `block.currentHash`：
   ```java
   String expectedHash = calculateHash(
       block.getEntityType(), block.getEntityId(), block.getAction(),
       block.getPreviousHash(), block.getDataSnapshot(), block.getTimestamp());
   if (!expectedHash.equals(block.getCurrentHash())) {
       // data_snapshot 或任意字段被篡改 → 重算结果不一致
       chainBroken = true;
   }
   ```
3. **签名验证**：用公钥验签 `block.currentHash` + `block.signature` — 检测签名是否伪造

返回 `IntegrityReport`（`intact: boolean` + `List<BlockCheckResult>`）。

提供 REST 端点：

| 端点 | 说明 |
|---|---|
| `GET /api/blockchain/verify/material` | 验证全局物料链 |
| `GET /api/blockchain/verify/batch?batchId=X` | 验证指定批次链 |
| `GET /api/blockchain/verify/all` | 验证所有批次链 |

---

## 四、触发方式（两种）

### 1. 领域事件监听（DDD v2 路径）

`@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` — 事务提交后异步追加。

| 监听器 | 事件 | entity_type |
|---|---|---|
| `BatchCreatedEventListener` | `BatchCreated` | PRODUCTION_BATCH |
| `GoodsReceivedEventListener` | `GoodsReceived` | STORAGE |
| `InspectionCompletedEventListener` | `InspectionCompleted` | INSPECTION |
| `TransportSaleRecordedEventListener` | `TransportSaleRecorded` | TRANSPORT_SALE |
| `MaterialChangedEventListener` | `MaterialChanged` | MATERIAL |
| `MaterialPurchaseChangedEventListener` | `MaterialPurchaseChanged` | MATERIAL_PURCHASE |

### 2. 服务层直接调用（旧路径）

`MaterialServiceImpl` 和 `MaterialPurchaseServiceImpl` 在 CRUD 操作中直接调用 `blockchainService.appendMaterialChainBlock()`。

### BlockchainInitializationService

`CommandLineRunner`，启动时运行。幂等检查：已有任何 `MATERIAL` 或 `BATCH` 链数据则跳过。遍历所有已有历史数据，逐条生成起源区块。

---

## 五、每日锚定

`BlockchainAnchorService` — `@Scheduled(cron = "0 0 3 * * ?")` 每天凌晨3点执行：

1. 锚定物料链最新哈希到 `blockchain_anchor` 表
2. 锚定每条批次链最新哈希到 `blockchain_anchor` 表
3. 将锚定记录签名后写入日志文件 `logs/blockchain-anchor-<yyyy-MM-dd>.log`
4. 日志行格式：`chainType | batchId/GLOBAL | hash | date | signature`

签名使用 `BlockchainService.sign()` 对整个行字符串签名，提供可独立验证的外部证据。

---

## 六、交易完整性关联

区块追加逻辑与业务操作在同一事务中提交（`@Transactional` + `@TransactionalEventListener`），确保：

- 业务数据写入成功 → 区块链日志写入成功（同一事务）
- 业务数据回滚 → 区块链日志不回滚（AFTER_COMMIT 阶段，仅在事务成功后触发）

---

## 七、完整性监控

### 整体流程

```
[前端 BlockchainMonitor.vue]
  ↓ GET /api/blockchain/monitor/summary  (30s 自动刷新)
[后端 BlockchainMonitorController]
  ↓
[BlockchainMonitorService.getSummary()]
  ├── blockchainService.verifyMaterialChain()  → 验证物料链
  └── blockchainService.verifyAllBatchChains() → 验证所有批次链
       ↓
  collect BrokenBlockDetail  ← 所有 !passed() 的区块
       ↓
  返回 BlockchainMonitorSummary
```

### BlockchainMonitorSummary 结构

```
BlockchainMonitorSummary
  ├── overallHealthy: boolean          ← materialReport.intact() && brokenCount == 0
  ├── materialChain: MaterialChainInfo
  │     ├── intact: boolean
  │     ├── blockCount: int
  │     └── lastAnchorDate: LocalDate  ← 来自 blockchain_anchor 表
  ├── batchChains: BatchChainSummary
  │     ├── totalBatches: long
  │     ├── intactCount: long
  │     ├── brokenCount: long
  │     ├── totalBlockCount: long
  │     ├── lastAnchorDate: LocalDate
  │     └── brokenBatchIds: List<Long>   ← 异常批次ID列表
  ├── brokenBlocks: List<BrokenBlockDetail>
  │     └── { blockId, batchId, entityType, entityId, action, errors }
  └── lastUpdated: LocalDateTime
```

### 异常原因分类（前端 errorLabel 映射）

| 数据库错误消息 | 前端展示 |
|---|---|
| `previous_hash mismatch` | 链断裂：前序哈希不匹配（数据链路被篡改） |
| `current_hash mismatch` | 数据被篡改：当前哈希与计算值不一致 |
| `signature verification failed` | 签名验证失败：区块签名无效（私钥不匹配或数据被篡改） |

### 前端监控面板

组件：`frontend/src/components/BlockchainMonitor.vue`

- 30 秒自动轮询 `GET /api/blockchain/monitor/summary`
- 顶部横幅：绿色（全部正常）/ 红色（存在异常）
- 两张状态卡：物料链摘要 + 批次链摘要（各统计字段）
- 异常详情区（仅在有异常时显示）：
  - **物料链异常**：el-table 列出所有异常区块（区块ID、操作、关联单据、错误原因）
  - **批次链异常**：el-collapse 按 batchId 分组折叠，每组一个 el-table
- 修复按钮：仅 SUPER_ADMIN 可见，调用确认对话框后执行修复

---

## 八、区块链修复

### 触发入口

1. **前端按钮**：监控面板中出现异常时显示"修复区块链"按钮
2. **API 直接调用**：`POST /api/blockchain/repair`（需 SUPER_ADMIN 角色）

### 修复流程（BlockchainRepairService.repairAll()）

```
repairAll()
  ├── repairMaterialChain()
  │     └── repairBlocks(materialChainBlocks)   ← 按 timestamp ASC 排序
  └── repairBatchChains()
        └── 按 batchId 分组
              └── repairBlocks(batchChainBlocks)  ← 每批次独立修复

repairBlocks(chainBlocks):
  for i = 0 to blocks.size():
    1. 修正 previousHash
       newPrevHash = (i > 0) ? blocks[i-1].currentHash : genesisHash
       block.setPreviousHash(newPrevHash)

    2. 用当前所有字段值重新计算 SHA-256
       newHash = calculateHash(
           entityType, entityId, action,
           newPrevHash, dataSnapshot, timestamp,
           batchId, operatorId, refMasterChainHash)

    3. 用当前 RSA 私钥重新签名
       newSignature = sign(newHash)

    4. block.setCurrentHash(newHash)
       block.setSignature(newSignature)
       blockchainLogRepo.save(block)
```

### 修复结果

```json
{
  "repaired": true,
  "materialBlocksFixed": 2,
  "batchBlocksFixed": 4,
  "totalFixed": 6
}
```

### 修复前提

- 修复的前提是 **data_snapshot 和 timestamp 等原始数据未被篡改**，否则重算的哈希值会与创建时不同（在创建时哈希正确的前提下修复才有意义）
- 首次部署后如遇哈希不匹配（如 LocalDateTime 精度丢失导致），运行一次修复即可恢复正常
- RSA 密钥变更后，所有现有区块签名失效，必须运行修复重新签名
- **持久化建议**：将 RSA 密钥文件（`backend/keys/private.pem`、`backend/keys/public.pem`）纳入版本管理或备份策略，避免重启后密钥变更导致签名集体失效

---

## 九、待完善

### 1. 锚定表应与业务库分离

当前 `blockchain_anchor` 表与 `blockchain_log` 表在同一 MySQL 实例（`food_traceability`）中，攻击者攻破数据库后可同时篡改两者，锚定作为独立验证层的意义被削弱。

**建议**：
- 将 `blockchain_anchor` 迁至独立 MySQL 实例（或 SQLite / 便宜的云数据库）
- 业务应用的 DB 账号对锚定库仅授予 `INSERT` + `SELECT` 权限，禁止 `UPDATE` / `DELETE`
- 定时任务每日凌晨写入锚定记录时通过独立数据源写入
- 攻击者需同时攻破两个数据库才能掩盖痕迹

当前已有文件系统锚定日志（`logs/blockchain-anchor-*.log`）+ RSA 签名作为兜底，但日志文件查询不便，独立数据库可兼顾安全性和可查询性。
