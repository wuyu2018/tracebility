# 食品溯源系统 Food Traceability System

轻量化学生比赛项目 - 食品溯源防伪验证系统

## 项目结构

```
food-traceability-system/
├── backend/                    # Spring Boot 3.2 后端
│   ├── src/main/java/         # Java 17 源代码
│   └── src/main/resources/    # 配置文件
│       ├── application.yml       # 默认配置
│       └── application-prod.yml  # 生产环境配置（覆盖默认）
├── frontend/                   # Vue 3 前端
│   ├── src/                   # 源代码
│   └── Dockerfile             # 构建产物 + Nginx 运行
├── scripts/                    # 部署脚本
│   ├── deploy.sh             # 一键部署
│   ├── backup.sh             # 数据库备份
│   └── health-check.sh       # 健康检查
├── docs/                      # 项目文档
├── docker-compose.prod.yml   # 生产 Docker Compose
├── edge-nginx.conf           # 边缘反向代理（TLS 终止）
├── .env.example              # 环境变量模板
└── README.md
```

## 功能说明

1. **防伪验证**：输入12-20位防伪码，POST 提交验证，返回完整溯源信息或伪品警示
2. **溯源链路**：产品信息 → 原料采购 → 仓储记录 → 出厂检验 → 运输销售 → 投诉记录；支持批次精准追溯
3. **扫码验证**：浏览器摄像头识别二维码，jsQR 解析，即时返回结果
4. **管理员工具**：独立页面 `/tools.html`，含数据录入、防伪码生成、数据总览、投诉管理
5. **权限控制**：运输销售等敏感字段仅管理员可见，公开扫码只显示运输时间和销售区域

## 核心机制

- **一次性防伪**：首次扫码记录查询时间，后续重复扫码标记 `valid:false` 并提示伪品风险
- **自动清理**：每日凌晨 3 点清理 7 天前查询数据及相关联的原料/仓储/检验/运输记录
- **多实例安全**：去重逻辑不依赖进程内缓存，使用数据库持久化，适合多实例部署
- **登录风控**：管理员登录需提交图片验证码 + 密码双重校验

## 安全加固（上线前已完成）

| 项 | 说明 |
|----|------|
| JWT 密钥 | 生产强制 `JWT_SECRET` 环境变量，无默认值，缺失即启动失败 |
| 管理员密码 | 首次启动无默认密码，必须设置 `DEFAULT_ADMIN_PASSWORD` 环境变量 |
| ddl-auto | 生产默认 `validate`，不自动改表结构 |
| CORS | 全局统一配置，移除 Controller 层 `@CrossOrigin("*")` |
| MySQL | `sslMode=PREFERRED`，禁用 `allowPublicKeyRetrieval` |
| 错误信息 | 生产不返回堆栈和异常详情，统一通用提示 |
| Nginx | CSP + HSTS + X-Frame-Options 安全头，gzip 压缩 |
| 依赖 | jjwt 升级至 0.12.6，CDN 依赖转为 npm 包 |

## 环境变量

| 变量名 | 说明 | 默认值 | 生产必填 |
|--------|------|--------|----------|
| `JWT_SECRET` | JWT 签名密钥（≥32字符） | dev 有 fallback，prod **无** | ✅ |
| `DEFAULT_ADMIN_PASSWORD` | 首次启动管理员密码 | 无（不设置则跳过创建） | 推荐 |
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL | `localhost:3306/food_traceability` | 推荐 |
| `SPRING_DATASOURCE_USERNAME` | MySQL 用户名 | `root` | 推荐 |
| `SPRING_DATASOURCE_PASSWORD` | MySQL 密码 | 空 | ✅ |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL 策略 | `validate` | - |
| `SPRING_REDIS_HOST` | Redis 地址 | `localhost` | - |
| `SPRING_REDIS_PASSWORD` | Redis 密码 | 空 | 推荐 |
| `MYSQL_ROOT_PASSWORD` | Docker MySQL root 密码 | - | ✅ |
| `MYSQL_DATABASE` | Docker 数据库名 | `food_traceability` | - |
| `MYSQL_USER` | Docker 应用用户 | `app_user` | - |
| `MYSQL_PASSWORD` | Docker 应用密码 | - | ✅ |
| `CORS_ALLOWED_ORIGINS` | 允许的跨域来源 | `*` | 推荐 |

## 快速启动

### Docker 部署（生产推荐）

```bash
cp .env.example .env
# 编辑 .env，必须设置 JWT_SECRET + MYSQL_ROOT_PASSWORD + MYSQL_PASSWORD

# 后台启动
docker-compose -f docker-compose.prod.yml up -d

# 查看状态
docker-compose -f docker-compose.prod.yml ps

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f backend
```

### 开发环境

#### 后端

```bash
cd backend
mvn spring-boot:run
```

后端运行在 http://localhost:8080

#### 前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 http://localhost:5173，自动代理 `/api` 到后端。

#### 访问地址

| 页面 | 地址 | 说明 |
|------|------|------|
| 首页 | http://localhost:5173/ | 主站 |
| 防伪验证 | http://localhost:5173/verify | 扫码验证 |
| 管理员工具 | http://localhost:5173/tools.html | 登录入口 |
| 管理后台 | http://localhost:5173/ToolsStandalone | 登录后跳转 |

## 生产部署

### 方式一：部署脚本

```bash
vim .env   # 配置生产环境变量
./scripts/deploy.sh
```

### 方式二：手动 Docker Compose

```bash
docker-compose -f docker-compose.prod.yml --env-file .env up -d
```

### 安全架构

```
                        [用户]
                           │
                           ▼
                    ┌──────────────┐
                    │  端口 80     │  HTTP → HTTPS 重定向
                    │  端口 443    │  TLS 终止（edge-nginx）
                    └──────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │  Edge Nginx  │  反向代理 + 安全头
                    └──────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
       ┌──────────────┐         ┌──────────────┐
       │   Frontend   │         │   Backend    │
       │   Nginx:80   │         │   Tomcat:8080│
       └──────────────┘         └──────┬───────┘
                                       │
                              ┌────────┴────────┐
                              ▼                 ▼
                       ┌──────────────┐ ┌──────────────┐
                       │    MySQL     │ │    Redis     │
                       │   端口 3306  │ │   端口 6379  │
                       └──────────────┘ └──────────────┘
                           容器内部网络，不暴露端口
```

### 端口策略

| 端口 | 服务 | 外部暴露 | 说明 |
|------|------|----------|------|
| 80 | Edge Nginx | ✅ | HTTP → HTTPS 重定向 |
| 443 | Edge Nginx | ✅ | TLS 终止，密文传输 |
| 3306 | MySQL | ❌ | 仅容器内部通信 |
| 6379 | Redis | ❌ | 仅容器内部通信 |
| 8080 | Backend | ❌ | 仅容器内部通信 |

### HTTPS 配置

1. 在 `certs/` 目录放证书：`fullchain.pem` + `privkey.pem`
2. `edge-nginx.conf` 已配置 TLS 1.2/1.3，自动启用
3. 配置后访问自动跳转 HTTPS，含 HSTS 头

## 数据库初始化

系统启动时自动完成：

```
MySQL 容器启动 → 创建数据库
Spring Boot 启动 → Hibernate 验证表结构（validate）
SmartDatabaseInitializer 检查 product 表 → 空则插入测试数据
```

**测试数据**：3 个产品（有机纯牛奶/橄榄油/蜂蜜）+ 关联原料/仓储/检验/运输记录

**管理员账号**：首次启动时通过 `DEFAULT_ADMIN_PASSWORD` 环境变量设置密码，用户名为 `admin`。不设置则不创建，需通过注册接口手动创建。

## 日志管理

| 文件 | 说明 |
|------|------|
| `/app/logs/application.log` | 应用主日志 |
| `/app/logs/error.log` | 仅 ERROR 级别 |

滚动策略：单文件 10MB，保留 30 天，总上限 500MB

```bash
docker exec food-traceability-backend tail -f /app/logs/application.log
```

## 健康检查

```bash
./scripts/health-check.sh
curl http://localhost:8080/actuator/health
```

## 技术栈

- **后端**：Spring Boot 3.2 / Java 17 / Spring Data JPA / MySQL 8.0 / Redis / jjwt 0.12.6
- **前端**：Vue 3 / Element Plus（按需导入）/ Vue Router / Axios / Vite 5 / jsQR / qrcode
- **容器化**：Docker / Docker Compose / Nginx（多阶段构建）
- **安全**：BCrypt 密码加密 / JWT 无状态认证 / CSP/HSTS 安全头
