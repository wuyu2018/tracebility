# 食品溯源系统 Food Traceability System

一款基于 DDD（领域驱动设计）架构的食品溯源防伪验证系统。

## 项目结构

```
food-traceability-system/
├── backend/                         # Spring Boot 后端
│   ├── src/main/java/com/foodtraceability/
│   │   ├── controller/              # 控制器层（API 接口）
│   │   ├── service/                # 服务层
│   │   │   ├── impl/              # 应用服务实现
│   │   │   └── domain/            # 领域服务
│   │   ├── domain/                # 领域层（DDD 核心）
│   │   │   ├── entity/            # 实体（聚合根）
│   │   │   ├── valueobject/       # 值对象
│   │   │   └── event/            # 领域事件
│   │   ├── repository/            # 仓储层
│   │   ├── dto/                  # 数据传输对象
│   │   ├── config/               # 配置类
│   │   ├── security/             # 安全认证
│   │   └── util/                 # 工具类
│   └── src/main/resources/
│       ├── application.yml        # 应用配置
│       └── logback-spring.xml     # 日志配置
├── frontend/                       # Vue 3 前端
│   ├── src/                       # Vue 源代码
│   └── Dockerfile                 # 前端容器配置
├── scripts/                        # 部署脚本
│   ├── deploy.sh                 # 一键部署脚本
│   ├── backup.sh                 # 数据库备份脚本
│   └── health-check.sh           # 健康检查脚本
├── docs/                          # 项目文档
│   └── api-documentation.md       # API 接口文档
├── docker-compose.prod.yml         # 生产环境 Docker 配置
├── .env.example                   # 环境变量模板
└── README.md
```

## 功能说明

1. **防伪验证**：消费者扫描产品二维码，输入防伪码验证，返回完整溯源信息或伪品警示
2. **溯源查询**：支持按防伪码或批次号查询完整链路（产品信息、原材料采购、仓储、检验、运输销售）
3. **产品管理**：CRUD 产品信息，关联批次和防伪码
4. **数据导入**：批量导入原材料、检验、仓储、运输销售数据
5. **投诉管理**：消费者提交投诉，管理员处理投诉
6. **管理员认证**：JWT 令牌认证，支持多管理员

## 核心机制

### 防伪验证与重复查询拦截

- 首次扫码验证成功，防伪码自动激活
- 重复查询时返回 `valid: false` 并提示伪品风险
- 验证码存储于 Redis，5 分钟有效期

### 管理员登录风控

- 登录失败 5 次后锁定账号 5 分钟
- 验证码存储在 Redis 中
- JWT 令牌认证

### 数据库初始化

- Hibernate `ddl-auto: update` 自动维护表结构
- `SmartDatabaseInitializer` 检查并插入初始数据

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2, Spring Security |
| ORM | Spring Data JPA, Hibernate |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| 认证 | JWT (jjwt) |
| 前端 | Vue 3, Vue Router, Axios, Vite |
| 容器化 | Docker, Docker Compose |
| 日志 | SLF4J + Logback |

### 后端架构（DDD）

```
┌─────────────────────────────────────────────────────────┐
│                    Controller Layer                      │
│  (AdminController, TraceabilityController, etc.)         │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                  Application Service                     │
│  (ProductApplicationService, TraceabilityApplication)    │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                         │
│  Entities │ Value Objects │ Domain Services │ Events   │
│  Product, ProductionBatch, SecurityCode, Material...    │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                  Repository Layer                       │
│  (ProductRepository, BatchRepository, etc.)             │
└─────────────────────────────────────────────────────────┘
```

## API 接口文档

完整接口文档请查看：[docs/api-documentation.md](docs/api-documentation.md)

### 公开接口（无需认证）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/verify` | GET | 防伪码验证（消费者扫码用） |
| `/api/trace/batch/{batchNumber}` | GET | 按批次号追溯 |
| `/api/products` | GET | 产品列表查询 |
| `/api/product-detail` | GET | 产品详情查询 |
| `/api/complaint` | POST | 提交投诉 |

### 管理接口（需认证）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/products` | POST/PUT/DELETE | 产品管理 |
| `/api/batches/**` | GET/POST | 批次管理 |
| `/api/materials/**` | GET/POST/PUT/DELETE | 原材料管理 |
| `/api/security-codes/**` | GET/POST | 防伪码管理 |
| `/api/admin/register` | POST | 管理员注册 |

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6+

### 后端启动

```bash
cd backend

# 1. 配置数据库连接（src/main/resources/application.yml）

# 2. 启动应用
mvn spring-boot:run
```

后端运行在 http://localhost:8080

### 前端启动

```bash
cd frontend

npm install
npm run dev
```

前端运行在 http://localhost:5173

### 访问地址

| 页面 | 地址 | 说明 |
|------|------|------|
| 首页 | http://localhost:5173/ | 主站 |
| 防伪验证 | http://localhost:5173/verify | 扫码验证页面 |
| 管理员登录 | http://localhost:5173/tools.html | 管理员入口 |

## Docker 部署

### 生产环境部署（推荐）

```bash
# 1. 复制环境配置
cp .env.example .env
# 编辑 .env 填写实际配置

# 2. 启动服务
docker-compose -f docker-compose.prod.yml up -d

# 3. 查看日志
docker-compose -f docker-compose.prod.yml logs -f backend
```

### 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| MYSQL_ROOT_PASSWORD | MySQL root 密码 | (必填) |
| MYSQL_DATABASE | 数据库名 | food_traceability |
| MYSQL_USER | 应用数据库用户 | app_user |
| MYSQL_PASSWORD | 应用数据库密码 | (必填) |
| SPRING_PROFILES_ACTIVE | Spring profiles | prod |
| CORS_ALLOWED_ORIGINS | CORS 允许来源 | * |
| FRONTEND_EXPOSED_PORT | 前端端口 | 80 |

## 数据库

### 表结构

| 表名 | 说明 |
|------|------|
| `product` | 产品表 |
| `production_batch` | 生产批次表 |
| `material_purchase` | 原材料采购表 |
| `security_code` | 防伪码表 |
| `inspection` | 检验记录表 |
| `storage` | 仓储记录表 |
| `transport_sale` | 运输销售表 |
| `batch_material_relation` | 批次-原材料关联表 |
| `complaint` | 投诉表 |
| `admin` | 管理员表 |

### 初始数据

系统启动时自动插入测试数据：
- 管理员账号：`admin` / `admin123`
- 3 个测试产品及其关联数据

## 日志

日志文件位置：`/app/logs/`

| 文件 | 说明 |
|------|------|
| application.log | 应用主日志 |
| error.log | 错误日志 |
| request.log | 请求日志 |

日志滚动策略：
- 单文件最大 10MB
- 保留 30 天
- 总体积上限 500MB

## 健康检查

```bash
# 端点检查
curl http://localhost:8080/actuator/health

# 脚本检查
./scripts/health-check.sh
```

## 开发指南

### 添加新的实体

1. 在 `domain/entity/` 创建实体类
2. 在 `repository/` 创建仓储接口
3. 在 `service/impl/` 创建应用服务
4. 在 `controller/` 创建控制器

### 日志规范

```java
log.info("[模块] 操作 - 参数: {}, 结果: {}", param, result);
log.warn("[模块] 警告 - 原因: {}", reason);
log.error("[模块] 错误 - 异常: ", e);
```

格式：`[时间] [线程] [级别] [Logger] - [模块] 消息`

## 许可证

MIT License
