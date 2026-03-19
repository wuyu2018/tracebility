# 食品溯源系统 Food Traceability System

轻量化学生比赛项目 - 食品溯源防伪验证系统

## 项目结构

```
food-traceability-system/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/         # Java 源代码
│   └── src/main/resources/    # 配置文件
│       ├── application.yml    # 开发环境配置
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
├── init.sql                   # 数据库初始化脚本
├── docker-compose.yml        # 开发环境 Docker 配置
├── docker-compose.prod.yml   # 生产环境 Docker 配置
├── .env.example              # 环境变量模板
└── README.md
```

## 功能说明

1. **防伪验证**：输入12-20位防伪码，通过 POST 提交到后端验证，返回完整溯源信息或伪品警示
2. **溯源信息**：产品信息、原料采购、贮存记录、出厂检验、储运销售、投诉信息
3. **采购联系**：点击导航栏「采购联系」按钮，浏览全部产品并联系采购
4. **防伪工具**：独立页面，不含导航栏和页脚，可生成防伪验证二维码及防伪码。访问地址：`/tools.html`

## 快速启动

### Docker 部署（推荐）

```bash
# 1. 复制环境配置
cp .env.example .env
# 编辑 .env 填写实际配置

# 2. 启动开发环境
docker-compose up -d

# 3. 查看服务状态
docker-compose ps

# 4. 查看日志
docker-compose logs -f backend
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
| SPRING_PROFILES_ACTIVE | Spring  profiles | prod |
| CORS_ALLOWED_ORIGINS | CORS 允许的来源 | * |
| FRONTEND_EXPOSED_PORT | 前端端口 | 80 |

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
