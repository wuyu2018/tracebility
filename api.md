# 食品安全溯源系统 — 后端 API 文档

> 基础路径: `/api`（Nginx 反向代理统一前缀）
> 认证方式: JWT Bearer Token（除标注 `公开` 的接口外均需认证）
> 响应格式: JSON

---

## 目录

1. [认证管理](#1-认证管理)
2. [产品管理](#2-产品管理)
3. [原料品种管理](#3-原料品种管理)
4. [原料采购管理](#4-原料采购管理)
5. [生产批次管理](#5-生产批次管理)
6. [仓储管理](#6-仓储管理)
7. [运输销售管理](#7-运输销售管理)
8. [质检管理](#8-质检管理)
9. [防伪码管理](#9-防伪码管理)
10. [产品-原料绑定](#10-产品-原料绑定)
11. [追溯查询](#11-追溯查询-公开)
12. [投诉管理](#12-投诉管理)
13. [区块链监控](#13-区块链监控)
14. [Agent 多智能体系统](#14-agent-多智能体系统)

---

## 1. 认证管理

**路径前缀:** `/api`

### POST `/captcha` — 存储验证码（公开）

在服务端存储验证码用于登录校验。

请求体:
```json
{
  "username": "admin",
  "captcha": "aB3xQ"
}
```

响应: `200 OK`（无响应体）

---

### POST `/login` — 管理员登录（公开）

请求体:
```json
{
  "username": "admin",
  "password": "password123",
  "captcha": "aB3xQ"
}
```

成功响应 `200 OK`:
```json
{
  "username": "admin",
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "role": "ADMIN",
  "agentType": "PRODUCTION"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `role` | string | `ADMIN` / `SUPER_ADMIN` |
| `agentType` | string/null | `PRODUCTION` / `CIRCULATION` / `SALES` / null（全权限） |

失败响应 `400 Bad Request`:
```
账号或密码错误
```

---

### POST `/admin/register` — 创建管理员（需 SUPER_ADMIN）

请求体:
```json
{
  "username": "newadmin",
  "password": "Pass@1234",
  "role": "ADMIN",
  "agentType": "PRODUCTION",
  "currentPassword": "当前管理员密码",
  "currentAdminUsername": "当前管理员用户名"
}
```

| 字段 | 说明 |
|------|------|
| `role` | `ADMIN` 或 `SUPER_ADMIN` |
| `agentType` | `PRODUCTION` / `CIRCULATION` / `SALES` / 空字符串（全权限） |
| `currentPassword` | 当前管理员密码（身份验证） |

成功 `201 Created`:
```json
{
  "message": "管理员创建成功",
  "username": "newadmin"
}
```

---

## 2. 产品管理

**路径前缀:** `/api`

### GET `/products` — 产品列表

Query: `?keyword=xxx`（可选，按名称搜索）

响应:
```json
[
  {
    "id": 1,
    "name": "有机牛奶",
    "specification": "250ml",
    "shelfLife": "12个月",
    "imageUrl": "http://...",
    "contactPhone": "13800138000",
    "contactEmail": "test@test.com",
    "qrCodeUrl": null
  }
]
```

### GET `/products/{id}` — 产品详情

### POST `/products` — 创建产品

请求体:
```json
{
  "name": "有机牛奶",
  "specification": "250ml",
  "shelfLife": "12个月",
  "imageUrl": "http://...",
  "contactPhone": "13800138000",
  "contactEmail": "test@test.com"
}
```

### PUT `/products/{id}` — 更新产品

### DELETE `/products/{id}` — 软删除产品

### DELETE `/products/{id}/hard` — 物理删除产品

### GET `/products/select` — 公开产品查询（公开）

Query: `?keyword=xxx&role=consumer`

### POST `/insert/products/list` — 获取产品列表（数据录入用）

### POST `/insert/products/generate-qrcode` — 为产品生成二维码

### POST `/insert/products/batch-generate-qrcode` — 批量生成二维码

请求体: `[1, 2, 3]`

### POST `/insert/products/batch-delete` — 批量删除（清除二维码）

请求体: `{"productIds": [1, 2, 3]}`

---

## 3. 原料品种管理

### 旧路径: `/api/material-varieties`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/material-varieties` | 列表 `?activeOnly=true` |
| GET | `/api/material-varieties/{id}` | 详情 |

### V2 路径: `/api/v2/material-varieties`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v2/material-varieties` | 创建品种 |
| GET | `/api/v2/material-varieties` | 列表 `?activeOnly=true/false` |
| GET | `/api/v2/material-varieties/{id}` | 详情 |
| PUT | `/api/v2/material-varieties/{id}` | 更新品种 |
| DELETE | `/api/v2/material-varieties/{id}` | 删除品种 |
| POST | `/api/v2/material-varieties/{id}/activate` | 启用品种 |
| POST | `/api/v2/material-varieties/{id}/deactivate` | 停用品种 |

创建/更新请求体:
```json
{
  "name": "有机生牛乳"
}
```

---

## 4. 原料采购管理

### 旧路径: `/api/materials`

### V2 路径: `/api/v2/material-purchases`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v2/material-purchases` | 创建采购单 |
| GET | `/api/v2/material-purchases` | 列表 `?materialId=1` |
| GET | `/api/v2/material-purchases/{id}` | 详情 |
| PUT | `/api/v2/material-purchases/{id}` | 更新采购单 |
| DELETE | `/api/v2/material-purchases/{id}` | 删除采购单 |

创建请求体:
```json
{
  "materialId": 1,
  "batchNumber": "MC-2025-001",
  "supplierName": "供应商名称",
  "producerName": "生产商名称",
  "producerAddress": "生产商地址",
  "purchaseDate": "2025-01-15T10:30:00",
  "quantity": 1000,
  "unit": "kg"
}
```

### 旧路径: `/api/insert/material-purchase` 和 `/api/insert/materials`（数据录入用）

---

## 5. 生产批次管理

### 旧路径: `/api/batches`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/batches` | 列表 `?productId=1` |
| GET | `/api/batches/{id}` | 详情 |
| GET | `/api/batches/by-number/{batchNumber}` | 按批次号查询 |

### V2 路径: `/api/v2`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v2/batches` | 创建批次 |

创建请求体:
```json
{
  "productId": 1,
  "productionDate": "2025-01-15",
  "shelfLife": "12个月",
  "quantity": 500,
  "unit": "箱",
  "materialPurchaseIds": [1, 2, 3]
}
```

成功 `201 Created`:
```json
{
  "id": 1,
  "batchNumber": "BAT-20250115-001",
  "productName": "有机牛奶"
}
```

---

## 6. 仓储管理

### V2 路径: `/api/v2`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v2/storage` | 录入仓储记录 |

请求体:
```json
{
  "batchId": 1,
  "storageTime": "2025-01-15T10:30:00",
  "outboundTime": "2025-01-20T14:00:00",
  "quantity": 500,
  "unit": "箱",
  "warehouseLocation": "A区-3号仓库"
}
```

### 旧路径: `/api/insert/storages`（列表查询）

---

## 7. 运输销售管理

### V2 路径: `/api/v2`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v2/transport-sales` | 录入运输/销售信息 |

请求体:
```json
{
  "batchId": 1,
  "transportCompany": "顺丰物流",
  "vehicleNumber": "京A·88888",
  "salesRegion": "华北区",
  "receiverName": "张三",
  "receiverContact": "13800138000",
  "recorderName": "李四",
  "environmentTemperature": 25.0,
  "productTemperature": 4.0,
  "time": "2025-01-20T14:00:00"
}
```

### 旧路径: `/api/insert/transport-sales`（列表查询）

---

## 8. 质检管理

### V2 路径: `/api/v2`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v2/inspections` | 完成质检报告 |

请求体:
```json
{
  "batchId": 1,
  "sampleName": "牛奶样品-A",
  "sampleQuantity": 5,
  "sampleSpecification": "250ml",
  "imageUrl": "http://...",
  "inspectorName": "王五",
  "qualified": true,
  "failReason": ""
}
```

### 旧路径: `/api/insert/inspections`（列表查询）

---

## 9. 防伪码管理

### 路径: `/api`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/batches/{id}/security-codes` | 生成防伪码 |
| GET | `/api/batches/{id}/security-codes` | 查询防伪码列表 |
| GET | `/api/security-codes/export/{batchId}` | 导出防伪码 |

生成请求体:
```json
{
  "quantity": 100
}
```

成功响应:
```json
{
  "count": 100,
  "codes": ["CODE001", "CODE002", ...]
}
```

---

## 10. 产品-原料绑定

### 路径: `/api`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/product-materials` | 绑定原料到产品 |
| GET | `/api/product-materials` | 查询绑定 `?productId=1` |
| DELETE | `/api/product-materials` | 解绑 `?productId=1&materialId=1` |
| PATCH | `/api/product-materials/{id}/visibility` | 切换可见性 |

---

## 11. 追溯查询（公开）

### 路径: `/api/v2/trace`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v2/trace/verify?code=xxx` | 防伪码查询 |
| GET | `/api/v2/trace/batch/{batchNumber}` | 批次号追溯 |

verify 成功响应:
```json
{
  "valid": true,
  "data": {
    "product": { "id": 1, "name": "有机牛奶", "specification": "250ml", ... },
    "batch": { "id": 1, "batchNumber": "BAT-xxx", ... },
    "materials": [{ "materialName": "生牛乳", "batchNumber": "MC-001", ... }],
    "inspection": { "sampleName": "牛奶样品", ... },
    "storage": { "storageTime": "...", "outboundTime": "...", "warehouseLocation": "..." },
    "transportSale": { "salesRegion": "华北区", ... },
    "status": "NORMAL",
    "repeatedQuery": false,
    "scanCount": 1,
    "firstScanTime": null,
    "queryTip": null
  }
}
```

重复查询响应:
```json
{
  "valid": false,
  "data": { ... },
  "message": "该产品已被查询过 2 次，首次查询时间...，该产品可能是伪品，请谨慎购买！"
}
```

---

## 12. 投诉管理

### 路径: `/api`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/complaint` | 公开 | 提交投诉 |
| GET | `/api/getAllComplaintInfo` | 认证 | 投诉列表 |
| DELETE | `/api/deleteComplaintInfo/{id}` | 认证 | 删除投诉 |
| DELETE | `/api/deleteComplaintInfo/batch` | 认证 | 批量删除投诉 |

提交投诉请求体:
```json
{
  "antiFakeCode": "CODE001",
  "complaintReason": "产品已过期",
  "productName": "有机牛奶",
  "batchNumber": "BAT-001"
}
```

---

## 13. 区块链监控

### 路径: `/api/blockchain`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/blockchain/public-key` | 获取区块链公钥 |
| GET | `/api/blockchain/monitor/summary` | 区块链完整性监控摘要 |

监控摘要响应:
```json
{
  "overallHealthy": true,
  "lastUpdated": "2025-01-15T10:30:00",
  "materialChain": {
    "intact": true,
    "blockCount": 42,
    "lastAnchorDate": "2025-01-15"
  },
  "batchChains": {
    "totalBatches": 10,
    "intactCount": 10,
    "brokenCount": 0,
    "totalBlockCount": 156,
    "lastAnchorDate": "2025-01-15"
  },
  "brokenBlocks": []
}
```

异常时 `brokenBlocks` 示例:
```json
[
  {
    "blockId": 5,
    "batchId": null,
    "action": "CREATE",
    "entityType": "MaterialPurchase",
    "entityId": 3,
    "errors": ["previous_hash mismatch"]
  }
]
```

错误类型: `previous_hash mismatch` / `current_hash mismatch` / `signature verification failed`

---

## 14. Agent 多智能体系统

### 路径: `/api/agent`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/agent/list` | 所有 Agent 列表 |
| GET | `/api/agent/{agentId}` | 单个 Agent 详情 |
| GET | `/api/agent/consensus/status` | 共识状态 |
| GET | `/api/agent/reputation/list` | 信誉列表 |
| GET | `/api/agent/{agentId}/reputation` | 单个 Agent 信誉详情 |

Agent 信息结构:
```json
{
  "agentId": "agent-prod-1",
  "agentType": "PRODUCTION",
  "state": "ACTIVE",
  "creditScore": 100,
  "registeredAt": "2025-01-01T00:00:00",
  "authorized": true,
  "metadata": { ... }
}
```

Agent 类型: `PRODUCTION`（生产方）/ `CIRCULATION`（流通方）/ `SALES`（销售方）/ `CA`（证书机构）

---

## 附录：权限矩阵

| 端点 | 公开 | ADMIN | SUPER_ADMIN | PRODUCTION | CIRCULATION | SALES |
|------|:----:|:-----:|:-----------:|:----------:|:-----------:|:-----:|
| `POST /api/captcha` | ✅ | | | | | |
| `POST /api/login` | ✅ | | | | | |
| `GET /api/v2/trace/**` | ✅ | | | | | |
| `POST /api/complaint` | ✅ | | | | | |
| `GET /api/products/select` | ✅ | | | | | |
| `POST /api/admin/register` | | | ✅ | | | |
| `/api/products/**` | | ✅ | ✅ | ✅ | | |
| `/api/v2/material-varieties/**` | | ✅ | ✅ | ✅ | | |
| `/api/v2/material-purchases/**` | | ✅ | ✅ | ✅ | | |
| `/api/v2/batches` | | ✅ | ✅ | ✅ | | |
| `/api/v2/storage` | | ✅ | ✅ | ✅ | | |
| `/api/v2/inspections` | | ✅ | ✅ | ✅ | | |
| `/api/v2/transport-sales` | | ✅ | ✅ | | ✅ | ✅ |
| `/api/batches/**/security-codes` | | ✅ | ✅ | ✅ | | |
| `/api/getAllComplaintInfo` | | ✅ | ✅ | | | |
| `/api/agent/**` | | ✅ | ✅ | | | |
| `/api/blockchain/**` | | ✅ | ✅ | | | |

> 注: 前端通过 `role` + `agentType` 联合控制标签页可见性，后端按 HTTP URL 模式进行认证拦截。`agentType` 为 null 的 ADMIN/SUPER_ADMIN 拥有全部操作权限。
