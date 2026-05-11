# Food Traceability — 系统架构概览

食品追溯系统。Spring Boot 3 + Vue 3 + MySQL + Redis。

---

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | Java 17, Spring Boot 3, Spring Data JPA, Spring Security |
| 前端 | Vue 3, Element Plus, Vite, Axios |
| 数据库 | MySQL 8, Redis (Lettuce) |
| 安全 | JWT, RSA-2048, SHA-256 |
| 构建 | Maven |
| 部署 | Docker Compose |

---

## 模块结构

```
backend/
├── src/main/java/com/foodtraceability/
│   ├── config/           # Spring 配置 (JWT, RSA, Redis, CORS, Security)
│   ├── controller/       # REST 控制器
│   ├── dto/              # 请求/响应 DTO
│   ├── entity/           # JPA 实体
│   ├── exception/        # BusinessException + 全局异常处理器
│   ├── repository/       # Spring Data JPA 仓库
│   ├── security/         # JWT 认证过滤器
│   ├── service/          # 业务逻辑 + 区块链服务
│   │   └── impl/         # 服务实现
│   └── traceability/     # DDD 追溯模块
│       ├── application/
│       │   ├── dto/      # 应用层 DTO
│       │   ├── event/    # 事件监听器 (→区块链写入)
│       │   └── service/  # 应用服务
│       ├── domain/
│       │   ├── event/    # 领域事件
│       │   ├── repository/
│       │   ├── service/  # 领域校验器
│       │   └── vo/       # 值对象
│       └── interfaces/
│           ├── dto/      # 接口层 DTO
│           └── rest/     # REST 控制器 (DDD 模块)
frontend/
├── src/
│   ├── views/            # 页面组件
│   ├── api/              # Axios API 调用
│   ├── router/           # Vue Router
│   └── utils/            # 工具函数
```

---

## 数据库表结构

详见 [docs/数据库设计.md](./docs/数据库设计.md)。

核心表链：`material → material_purchase → production_batch → [storage, inspection, transport_sale] → security_code → complaint`

辅助表：`blockchain_log`, `blockchain_anchor`, `admin`, `product_material_relation`, `batch_material_relation`

---

## 区块链防篡改

详见 [docs/区块链设计.md](./docs/区块链设计.md)。

双链架构：
- **MATERIAL 链**：全局单链，记录原料/采购的创建与变更
- **BATCH 链**：每批次独立链，记录全生命周期（生产→入库→检验→运输）

SHA-256 哈希链 + RSA-2048 签名。事件驱动通过 `@TransactionalEventListener` 自动追加区块。每天凌晨 3:00 日终锚定。

验证 API：`/api/blockchain/verify/*`

---

## 认证与安全

- JWT 登录 (`/api/admin/login`)，24 小时过期
- 密码：BCrypt 加密，最少 8 位含字母+数字+特殊字符
- 登录限流：Redis 记录尝试次数（最多 6 次）
- 登录验证码
- 区块链签名 RSA 密钥对：支持 PEM 文件加载或启动时自动生成

---

## 核心业务流程

### 追溯查询
```
防伪码 → 生产批次 → [入库, 检验, 运输销售]
                 → 原料采购[] → 原料[]
```

### 投诉流程
```
防伪码扫码 → 首次激活
           → 重复扫码标记异常
           → 投诉关联防伪码 → 关联批次信息
```

### 检验不合格流程
```
检验结果 = 不合格
  → 冻结该批次所有防伪码
  → 区块链记录追加
```

---

## API 端点

| 前缀 | 用途 |
|---|---|
| `/api/admin` | 认证 (登录/登出) |
| `/api/material` | 原料 CRUD |
| `/api/material-purchase` | 采购 CRUD (软删除) |
| `/api/product` | 产品 CRUD |
| `/api/production-batch` | 批次管理 |
| `/api/storage` | 入库记录 |
| `/api/inspection` | 检验记录 |
| `/api/transport-sale` | 运输销售记录 |
| `/api/security-code` | 防伪码生成与查询 |
| `/api/complaint` | 投诉提交 |
| `/api/traceability` | 根据防伪码追溯 |
| `/api/blockchain/verify/*` | 区块链完整性验证 |

---

## 部署

- `docker-compose.yml` / `docker-compose.prod.yml`
- MySQL + Redis + backend JAR
- CORS 可通过 `CORS_ALLOWED_ORIGINS` 配置

---

## DDD 重构（traceability 包）

`traceability` 子包是正在进行的 DDD 重构：

- `domain/event/` — 领域事件与值对象
- `application/event/` — 事务事件监听器（当前用于区块链写入）
- `interfaces/rest/` — REST 控制器

旧 CRUD 服务（`service/`、`controller/`）在过渡期并存。
