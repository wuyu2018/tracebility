# 食品溯源系统 Food Traceability System

轻量化学生比赛项目 - 食品溯源防伪验证系统

## 项目结构

```
food-traceability-system/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/         # Java 源代码
│   │   └── com/foodtraceability/config/
│   │       └── SmartDatabaseInitializer.java  # 智能数据库初始化器
│   └── src/main/resources/    # 配置文件
│       ├── application-prod.yml  # 生产环境配置
│       └── logback-spring.xml # 日志配置
├── frontend/                   # Vue 3 前端
│   ├── src/                   # Vue 源代码
│   └── Dockerfile            # 前端容器配置
├── scripts/                    # 部署脚本
│   ├── deploy.sh             # 一键部署脚本
│   ├── backup.sh             # 数据库备份脚本
│   └── health-check.sh       # 健康检查脚本
├── docs/                      # 项目文档
├── docker-compose.prod.yml   # 生产环境 Docker 配置
├── .env.example              # 环境变量模板
└── README.md
```

## 功能说明

1. **防伪验证**：输入12-20位防伪码，通过 POST 提交到后端验证，返回完整溯源信息或伪品警示
2. **溯源信息**：产品信息、原料采购、贮存记录、出厂检验、储运销售、投诉信息；支持批次模式精确返回对应批次的完整链路
3. **采购联系**：点击导航栏「采购联系」按钮，浏览全部产品并联系采购
4. **防伪工具**：独立页面，不含导航栏和页脚，可生成防伪验证二维码及防伪码。访问地址：`/tools.html`

## 核心机制（上线重点）
- **一次性防伪验证与重复查询拦截**：后端通过把 `Product.lastQueriedTime` 写入数据库来判定“是否已被查询过”。首次验证成功后，后续同一防伪码（批次模式下同一批次）再次访问会返回 `valid:false` 并提示已被查询过。
- **自动清理（节省隐私/数据空间）**：后端使用定时任务（`@Scheduled(cron="0 0 3 * * ?")`，每天凌晨 3 点）清理 7 天前的查询数据；清理产品的原料采购/贮存/检验/储运销售，并且仅在“该产品下已无未删除批次”时才删除按 `productName` 关联的投诉数据，避免误删仍在保留期内的批次相关信息。
- **服务端多端一致性**：去重逻辑不依赖进程内缓存，便于多实例部署保持一致拦截效果。
- **前端扫码识别**：`Verify` 页面使用摄像头获取图像，并用 `jsQR` 解析二维码内容（动态导入，支持从摄像头直接读取防伪码）。
- **管理员登录风控**：管理员登录接口要求提交验证码（`POST /api/captcha` 先存储验证码，`POST /api/login` 同时校验验证码与密码）。

## 关键接口（前后端对照）
- 防伪验证
  - `POST /api/verify`：请求体包含 `antiFakeCode`，可选 `batchNumber`
  - `GET /api/verify?code=...`：用于扫描二维码时的“快速验证”
- 采购信息
  - `POST /api/purchase-info`
- 产品列表（用于采购联系页和数据录入页的产品下拉）
  - `POST /api/insert/products/list`
- 投诉
  - `POST /api/complaint`
  - `GET /api/getAllComplaintInfo`
  - `DELETE /api/deleteComplaintInfo/{id}`
  - `DELETE /api/deleteComplaintInfo/batch`（请求体为 `Long[]`）
- 管理员
  - `POST /api/captcha`（提交 `username`/`captcha`）
  - `POST /api/login`（请求体为 `AdminLoginDTO`，包含 `captcha`）
  - `POST /api/admin/register`

## 快速启动

### Docker 部署（推荐）

```bash
# 1. 复制环境配置
cp .env.example .env
# 编辑 .env 填写实际配置

# 2. 启动（生产配置）
docker-compose -f docker-compose.prod.yml up -d

# 3. 查看服务状态
docker-compose -f docker-compose.prod.yml ps

# 4. 查看日志
docker-compose -f docker-compose.prod.yml logs -f backend
```

### 开发环境

#### 后端

```bash
cd backend
mvn spring-boot:run
```

后端将运行在 http://localhost:8080

#### 前端

```bash
cd frontend
npm install
npm run dev
```

前端将运行在 http://localhost:5173，并代理 `/api` 到后端。

## 生产部署

### 方式一：使用部署脚本

```bash
# 编辑 .env 配置
vim .env

# 执行部署
./scripts/deploy.sh
```

### 方式二：手动部署

```bash
# 使用生产配置启动
docker-compose -f docker-compose.prod.yml --env-file .env up -d
```

### 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| MYSQL_ROOT_PASSWORD | MySQL root 密码 | (必填) |
| MYSQL_DATABASE | 数据库名 | food_traceability |
| MYSQL_USER | 应用数据库用户 | app_user |
| MYSQL_PASSWORD | 应用数据库密码 | (必填) |
| SPRING_PROFILES_ACTIVE | Spring profiles | prod |
| SPRING_JPA_HIBERNATE_DDL_AUTO | Hibernate 策略 | update |
| CORS_ALLOWED_ORIGINS | CORS 允许的来源 | * |
| FRONTEND_EXPOSED_PORT | 前端端口 | 80 |

### 数据库初始化

系统启动时自动完成三层检测和创建：

| 层级 | 负责方 | 实现方式 |
|------|--------|----------|
| 数据库 | MySQL 容器 | `MYSQL_DATABASE` 环境变量指定，容器启动时自动创建 |
| 表结构 | Hibernate | `ddl-auto: update`，表不存在时自动创建 |
| 初始数据 | SmartDatabaseInitializer | 检查 `product` 表是否有数据，无数据则插入 |

**启动流程**：
```
MySQL 容器启动
    ↓ 自动创建 food_traceability 数据库
Spring Boot 启动
    ↓ Hibernate 检测表是否存在
表不存在 → 自动创建所有表
    ↓
SmartDatabaseInitializer 检查 product 表
    ↓ 无数据
插入初始数据（3个产品 + 关联数据 + admin账号）
    ↓ 有数据
跳过初始化，保留现有数据
```

**场景说明**：

| 场景 | 行为 |
|------|------|
| 首次部署（数据库为空） | 自动创建数据库 + 表 + 插入初始数据 |
| 重新部署（数据库已存在） | 跳过初始化，**保留所有现有数据** |
| 表结构变更 | Hibernate 自动更新表结构 |

**初始数据包括**：
- 3 个测试产品（有机纯牛奶、有机橄榄油、有机蜂蜜）
- 关联的原料采购、贮存、出厂检验、储运销售、投诉数据
- 管理员账号：`admin` / `admin123`

**Hibernate 策略说明**：
- `update`：仅更新表结构，不删除现有数据（**推荐**）
- `validate`：仅验证表结构，不做任何更改
- `create`：每次启动创建新表（**会清空数据**）
- `create-drop`：启动时创建，关闭时删除

### 端口配置

#### 端口说明

| 端口 | 服务 | 内部/外部 | 生产环境建议 |
|------|------|-----------|--------------|
| 80 | Edge 网关 (HTTP重定向) | 外部暴露 | **必须开放** - 自动跳转到 HTTPS |
| 443 | Edge 网关 (HTTPS) | 外部暴露 | **必须开放** - 密文传输 |
| 3306 | MySQL | **不暴露** | **禁止开放** - 仅容器内部通信 |
| 8080 | Backend | **不暴露** | **禁止开放** - 仅容器内部通信 |
| 5173 | Vite Dev | 外部暴露 | 仅开发环境使用 |

#### 安全架构

```
                        [用户]
                           │
                           ▼
                    ┌──────────────┐
                    │   端口 80    │  ◀── 防火墙开放（重定向）
                    │   端口 443   │  ◀── 密文传输（HTTPS）
                    └──────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │    Edge Nginx │  (TLS 终止 + 转发到 Frontend)
                    │   端口 80    │
                    └──────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
       ┌──────────────┐         ┌──────────────┐
       │   Backend    │         │    MySQL     │
       │   端口 8080  │         │   端口 3306  │
       └──────────────┘         └──────────────┘
              ▲                         ▲
              │      容器内部网络        │
              └─────────────────────────┘
```

#### 自定义端口

如需修改外部暴露端口，编辑 `.env`：

```bash
# .env
FRONTEND_EXPOSED_PORT=8080
```

#### 常见云服务商端口配置

| 云服务商 | 需要额外开放的端口 |
|----------|-------------------|
| 阿里云 ECS | 80, 443 (如需 HTTPS) |
| 腾讯云 CVM | 80, 443 |
| AWS EC2 | 80, 443 |
| 华为云 ECS | 80, 443 |

**注意**：数据库端口 (3306) 和后端端口 (8080) **不要**在云服务商安全组中开放。

### 日志管理

日志文件位置：`/app/logs/`

| 文件 | 说明 |
|------|------|
| application.log | 应用主日志 |
| error.log | 错误日志（仅 ERROR 级别） |
| request.log | 请求日志 |

日志滚动策略：
- 单文件最大 10MB
- 保留 30 天
- 总体积上限 500MB

查看实时日志：
```bash
docker exec food-traceability-backend tail -f /app/logs/application.log
```

### 健康检查

```bash
# 执行健康检查
./scripts/health-check.sh

# 手动检查端点
curl http://localhost:8080/actuator/health
```

## 测试数据

系统预置了 3 个测试产品的防伪码：

| 防伪码 | 产品 |
|--------|------|
| AF202400012345 | 有机纯牛奶 |
| AF202400067890 | 有机橄榄油 |
| AF202400099999 | 有机蜂蜜 |

## 技术栈

- **后端**：Spring Boot 3.2, Spring Data JPA, MySQL 8.0
- **前端**：Vue 3, Vue Router, Axios, Vite
- **容器化**：Docker, Docker Compose
- **日志**：SLF4J + Logback

## 日志规范

### 日志级别使用

| 级别 | 使用场景 |
|------|----------|
| DEBUG | 开发调试信息，生产环境不输出 |
| INFO | 正常业务操作，如登录、验证、数据录入 |
| WARN | 警告信息，如验证失败、参数错误 |
| ERROR | 错误信息，如系统异常、数据库错误 |

### 日志格式

```
2024-01-15 10:30:45.123 [http-nio-8080-exec-1] INFO  c.f.c.TraceabilityController - [防伪验证] 验证成功 - 防伪码: AF20****2345, 产品: 有机纯牛奶, 耗时: 45ms
```

格式说明：`时间 [线程名] 级别 logger名 - [模块] 消息`

---

## 正式上线指南

### 购买域名和服务器后的修改

购买域名和服务器后，需修改以下配置文件：

| 文件 | 修改内容 |
|------|----------|
| `.env` | 设置 `MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD`、`CORS_ALLOWED_ORIGINS=https://你的域名` |
| `docker-compose.prod.yml` | 修改 `FRONTEND_EXPOSED_PORT`（外部 80 入口；会重定向到 443） |
| `frontend/nginx.conf` | `server_name` 改为你的域名 |
| `backend/src/main/resources/application-prod.yml` | `app.verify-url` 改为你的域名 |

### HTTPS 配置（如需）

如需启用 HTTPS（TLS 终止），建议使用前置 Nginx 网关容器（`edge`）：

在本项目的 `docker-compose.prod.yml` 中，已集成前置网关：
- 网关配置文件：`edge-nginx.conf`
- 证书挂载目录：`./certs`
- 请在项目根目录创建 `certs/` 目录，并放置证书文件
- 证书文件名约定：
  - `certs/fullchain.pem`
  - `certs/privkey.pem`

启用后：
- 外网访问走 `https://`（密文传输）
- 前端容器与后端容器间仍走容器内 `http`（降低证书运维复杂度）

### 一键部署清单

```bash
# 1. 上传代码到服务器
git clone 你的仓库

# 2. 配置环境变量
cp .env.example .env
vim .env  # 填写实际密码和域名

# 3. 启动服务
docker-compose -f docker-compose.prod.yml --env-file .env up -d
```

### 上线后可删除的文件

上线稳定运行后，以下文件可删除以精简项目：

| 文件 | 原因 | 建议 |
|------|------|------|
| `.env.example` | 仅作环境变量模板；生产以 `.env` 为准 | 可按需保留/删除 |

### 精简后的项目结构

```
food-traceability-system/
├── backend/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/resources/
│       └── application-prod.yml    # 仅生产配置
├── frontend/
│   ├── src/
│   ├── Dockerfile
│   └── nginx.conf
├── scripts/
│   ├── deploy.sh
│   ├── backup.sh
│   └── health-check.sh
├── docker-compose.prod.yml         # 仅生产配置
├── .env                           # 敏感配置，不提交
└── README.md
```

### .gitignore 追加

上线后可将以下内容追加到 `.gitignore`：

```gitignore
# 上线后删除
.env.example
```
