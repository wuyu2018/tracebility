# 食品溯源系统 Food Traceability System

轻量化学生比赛项目 - 食品溯源防伪验证系统

## 项目结构

```
food-traceability-system/
├── backend/          # Spring Boot 后端
├── frontend/         # Vue 3 前端
├── docs/
├── scripts/
└── README.md
```

## 功能说明

1. **防伪验证**：输入12-20位防伪码，通过 POST 提交到后端验证，返回完整溯源信息或伪品警示
2. **溯源信息**：产品信息、原料采购、贮存记录、出厂检验、储运销售、投诉信息
3. **采购联系**：点击导航栏「采购联系」按钮，浏览全部产品并联系采购
4. **防伪工具**：独立页面，不含导航栏和页脚，可生成防伪验证二维码及防伪码。访问地址：`/tools.html`

## 快速启动

### 后端

```bash
cd backend
mvn spring-boot:run
```

后端将运行在 http://localhost:8080

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端将运行在 http://localhost:5173，并代理 `/api` 到后端。

## 测试数据

系统预置了 3 个测试产品的防伪码：

| 防伪码 | 产品 |
|--------|------|
| AF202400012345 | 有机纯牛奶 |
| AF202400067890 | 有机橄榄油 |
| AF202400099999 | 有机蜂蜜 |

## 技术栈

- **后端**：Spring Boot 3.2, Spring Data JPA, H2 内存数据库
- **前端**：Vue 3, Vue Router, Axios, Vite
