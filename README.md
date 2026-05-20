# Food Traceability 食品安全追溯系统

基于 Spring Boot 3 + Vue 3 的食品安全追溯平台，集成类区块链数据防篡改机制，支持从原料到销售的全链路追溯。

## 架构概览

```
[消费者扫码] → [Edge Nginx] → [前端 Vue 3] → [后端 Spring Boot 3]
                                                    │
                                                    ├── MySQL（业务数据 + 区块链账本）
                                                    ├── Redis（登录防暴、缓存）
                                                    └── MySQL（锚定库 — 操作日志 + 每日快照）
```

## 核心技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17, Spring Boot 3.2, Spring Security 6, JPA / Hibernate |
| 前端 | Vue 3, Element Plus, Vite |
| 数据库 | MySQL 8.0（主库 + 锚定库双实例） |
| 缓存 | Redis 7（登录防暴） |
| 部署 | Docker Compose, Nginx（边缘代理 + 前端容器） |
| 安全 | JWT（HMAC-SHA384）, RSA-2048 签名, 角色分级 |

## 核心功能

### 防伪追溯
- 消费者扫码查产品溯源信息（原料 → 生产 → 仓储 → 检验 → 销售）
- 防伪码状态追踪（首次/重复扫码检测）
- 投诉举报入口

### 数据防篡改（类区块链）
- **双链结构**：全局物料链（MATERIAL）+ 每批次独立链（BATCH），通过 `ref_master_chain_hash` 交叉链接
- **SHA-256 哈希链**：每个区块的 `previous_hash` 指向前一区块，形成单向链表
- **RSA 签名**：每个区块的 `current_hash` 经 SHA256withRSA 签名，私钥签名、公钥验签
- **每日锚定**：凌晨 3 点定时快照链状态，写入独立库 + 签名日志文件，提供外部可验证证据

### 监控与修复
- 管理员后台实时监控面板（30 秒自动刷新）
- 链完整性检测（前向链接 / 哈希比对 / 签名验证）
- 一键修复异常区块（重算哈希 + 重新签名）

### 角色分级
| 角色 | 权限 |
|---|---|
| `ADMIN` | 业务管理、链验证、监控查看 |
| `SUPER_ADMIN` | 区块链修复、管理员注册 |

## 快速启动

### 前置要求
- Docker & Docker Compose
- OpenSSL（生成 RSA 密钥）

### 1. 生成 RSA 密钥

```bash
mkdir -p backend/keys
openssl genrsa -out backend/keys/private.pem 2048
openssl rsa -in backend/keys/private.pem -pubout -out backend/keys/public.pem
```

### 2. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env，修改所有密码（JWT_SECRET、MYSQL_*、REDIS_PASSWORD、ANCHOR_MYSQL_*）
```

### 3. 启动

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

### 4. 访问

- 前端页面：`http://localhost`
- 管理员登录：`/admin/login`

## 项目结构

```
├── backend/                     # Spring Boot 后端
│   ├── src/main/java/.../
│   │   ├── config/              # 安全、Redis、RSA 配置
│   │   ├── controller/          # REST 控制器
│   │   ├── service/             # 业务逻辑
│   │   ├── repository/          # 主库 JPA 仓库
│   │   ├── entity/              # 主库实体
│   │   ├── anchor/              # 锚定库（独立数据源）
│   │   │   ├── config/          # 多数据源配置（主库 + 锚定库）
│   │   │   ├── entity/          # 锚定实体（BlockchainAnchor, OperationLog）
│   │   │   └── repository/      # 锚定仓库
│   │   ├── aop/                 # 操作日志切面
│   │   └── dto/                 # 数据传输对象
│   ├── docker-compose.yml       # 后端服务定义
│   ├── Dockerfile               # 多阶段构建
│   └── keys/                    # RSA 密钥对（已 gitignore）
├── frontend/                    # Vue 3 前端
│   ├── src/
│   │   ├── components/          # 组件
│   │   ├── services/            # API 封装
│   │   └── utils/               # 工具（axios 实例等）
│   ├── edge-nginx.conf          # 边缘 Nginx 配置
│   └── nginx.conf               # 前端容器 Nginx 配置
├── scripts/                     # 部署运维脚本
│   ├── anchor-init.sh           # 锚定库初始化
│   ├── backup.sh                # 数据库备份
│   └── deploy.sh                # 部署脚本
├── docker-compose.prod.yml      # 生产编排（入口）
└── .env                         # 环境变量（已 gitignore）
```

## 数据库架构

双 MySQL 实例设计：

| 实例 | 用途 | 表 |
|---|---|---|
| 主库 | 业务数据 + 区块链账本 | `production_batch`, `product`, `blockchain_log`, `blockchain_retry_task` 等 |
| 锚定库 | 审计日志 + 每日快照 | `operation_log`, `blockchain_anchor` |

锚定库应用账号仅授予 `SELECT, INSERT` 权限，无法修改或删除已有记录。

## API 端点

### 区块链相关

| 方法 | 路径 | 说明 | 角色 |
|---|---|---|---|
| GET | `/api/blockchain/verify/material` | 验证物料链 | ADMIN |
| GET | `/api/blockchain/verify/batch` | 验证指定批次链 | ADMIN |
| GET | `/api/blockchain/verify/all` | 验证所有批次链 | ADMIN |
| GET | `/api/blockchain/monitor/summary` | 监控摘要 | ADMIN |
| GET | `/api/blockchain/public-key` | 获取 RSA 公钥 | ADMIN |
| POST | `/api/blockchain/repair` | 修复异常区块 | SUPER_ADMIN |

### 其他
参见 Controller 层注解文档。

## 安全说明

- `.env`、`backend/keys/`、`certs/` 中的私钥文件已配置 gitignore，不会提交
- 锚定库与应用库物理隔离，攻击者需同时攻破两个数据库才能掩盖痕迹
- 操作日志（`operation_log`）写入失败不阻塞业务操作
- JWT Token 24 小时过期，含角色声明用于权限控制

## 区块链设计文档

参见 [docs/blockchain-logic.md](docs/blockchain-logic.md)（需从 git 检出或本地查看）。
