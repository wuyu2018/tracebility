# 日志与操作记录机制

## 概述

项目中不存在独立的 `operation_log` 或 `audit_log` 数据表。持久化的操作审计完全依赖 `blockchain_log`（详见 `blockchain-logic.md`）。除此之外的日志分为四层：**应用日志**（SLF4J）、**扫码追踪**、**登录防暴**、**投诉记录**。

---

## 一、应用日志（SLF4J）

使用 `LoggerFactory.getLogger()`，无 AOP 日志切面，无请求/响应拦截器。各模块使用统一的中文前缀标识日志来源。

### 控制器层

| 类 | 日志前缀 | 记录内容 |
|---|---|---|
| `DataManagementController` | `[产品管理]` `[原材料管理]` `[检验检测]` `[仓储]` `[运输销售]` `[防伪码管理]` `[产品详情]` `[管理员产品详情]` `[产品选择]` `[数据导入]` | 业务操作的名称、ID、执行结果（成功/失败 + 异常信息） |
| `AdminController` | `[管理员登录]` | 登录请求、登录成功、登录失败（含耗时） |
| `ComplaintController` | `[投诉提交]` | 投诉创建（含耗时） |
| `GetAllComplaintInfoController` | `[投诉查询]` `[投诉删除]` `[投诉批量删除]` | 查询、删除、批量删除操作及耗时 |
| `BlockchainController` | 无前缀 | 链验证请求和结果 |
| `TraceabilityQueryController` | 无前缀 | 扫码查询请求 |

### 安全层

| 类 | 日志前缀 | 记录内容 |
|---|---|---|
| `JwtAuthenticationFilter` | `[JWT 过滤器]` | Token 验证成功（debug）、验证失败（error） |
| `GlobalExceptionHandler` | `[业务异常]` `[访问拒绝]` `[参数验证]` `[系统异常]` | 所有未处理的异常分类记录 |

### 区块链相关

| 类 | 日志前缀 | 记录内容 |
|---|---|---|
| `BlockchainInitializationService` | `[BlockchainInit]` | 区块链初始化跳过、进度、完成 |
| `BlockchainAnchorService` | 无前缀 | 每日锚定操作 |

### 其他

| 类 | 日志内容 |
|---|---|
| `AdminInitializer` | 默认管理员创建日志 |
| `LoginAttemptService` | 登录失败、锁定、解锁日志 |
| 各 DDD ApplicationService | `[V2 ...]` `[Event]` `[Blockchain]` 前缀的操作日志 |

---

## 二、扫码追踪（SecurityCode）

每次消费者扫码查询时触发状态变更，记录在 `security_code` 表。

### 触发点

`TraceabilityQueryApplicationService.queryByCode()` / `TraceabilityServiceImpl.getTraceInfoByCode()`

```java
// SecurityCode.java
public void recordQueryAndActivateIfNeeded() {
    if (isFirstQuery()) {
        activate();   // state: 未激活 → 已激活, 记录 firstScanTime, scanCount = 1
    } else {
        recordQuery(); // scanCount++
    }
}
```

### 记录字段

| 字段 | 说明 |
|---|---|
| `code` | 防伪码 |
| `status` | 未激活 / 已激活 / 已冻结 |
| `first_scan_time` | 首次扫码时间 |
| `scan_count` | 扫码次数 |
| `created_at` | 创建时间 |

### 前端提示

重复扫码（`scanCount > 1`）时返回：

```
该产品已被查询过 N 次，首次查询时间：xxx。重复查询可能是伪品，请谨慎购买！
```

---

## 三、登录防暴机制（Redis）

### LoginAttemptService

存储于 Redis，非持久化。

| Key | TTL | 用途 |
|---|---|---|
| `login_failed:<username>` | 1小时 | 失败计数 |
| `locked:<username>` | 30分钟 | 锁定标记 |

规则：
- 连续失败 **5 次** → 锁定账户 30 分钟
- 成功登录 → 清除失败计数
- 锁定期间拒绝登录

### 触发点

`AdminController.login()` 调用 `loginAttemptService.loginSucceeded()` / `loginFailed()`。

---

## 四、投诉记录

### Complaint → 表 `complaint`

| 字段 | 说明 |
|---|---|
| `id` | 主键自增 |
| `security_code_id` | 关联的防伪码ID（FK → security_code） |
| `complaint_reason` | 投诉原因 |
| `complaint_time` | 投诉时间 |

### API

| 端点 | 说明 |
|---|---|
| `POST /api/complaint` | 提交投诉 |
| `GET /api/getAllComplaintInfo` | 查询所有投诉 |
| `DELETE /api/deleteComplaintInfo/{id}` | 删除单条投诉 |
| `DELETE /api/deleteComplaintInfo/batch` | 批量删除投诉 |

注：投诉记录**不入区块链**，仅存于 complaint 表。

---

## 五、每日锚定日志（文件落盘）

`BlockchainAnchorService` 每天凌晨 3 点执行定时任务，除了写入 `blockchain_anchor` 表外，还会在**文件系统**生成签名日志：

```
logs/blockchain-anchor-2026-05-11.log
```

每行格式：
```
chainType | batchId/GLOBAL | hash | date | RSA-signature
```

整行内容使用 RSA 签名，用于提供独立于数据库的可验证外部证据。

---

## 汇总

| 机制 | 存储位置 | 触发方式 | 是否可篡改 |
|---|---|---|---|
| 区块链操作日志 | `blockchain_log` 表 | 业务操作完成事件（@TransactionalEventListener） | 是（SHA-256 + RSA） |
| 每日锚定 | `blockchain_anchor` 表 + 签名文件 | 定时任务凌晨 3 点 | 是（RSA 签名） |
| 扫码追踪 | `security_code` 表 | 每次扫码查询 | 否（scan_count 可累加） |
| 登录防暴 | Redis | 每次登录失败/成功 | 否（内存临时存储） |
| 投诉记录 | `complaint` 表 | 用户提交投诉 | 否 |
| 应用日志 | stdout / 日志文件 | 贯穿执行过程 | 否 |

### 已知不足

- `blockchain_log` 中 `operator_id` 始终为 null，未记录操作人
- 无法通过 AOP 切面统一记录方法级调用日志
- 投诉记录未纳入区块链审计
