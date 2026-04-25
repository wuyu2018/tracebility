# JWT 前端集成说明

## 概述

本文档说明前端如何与后端 JWT 认证机制集成，实现完整的认证流程。

## 认证流程

```
用户登录 → 验证验证码 → 获取 JWT Token → 保存 Token → 请求自动附加 Token → Token 过期处理 → 退出登录
```

## 核心组件

### 1. Token 管理工具 (`utils/auth.js`)

提供 Token 的存储、读取、验证和清除功能：

```javascript
// 保存 Token
setToken(token, tokenType = 'Bearer')

// 获取 Token
getToken()

// 检查是否已登录
isAuthenticated()

// 检查 Token 是否过期
isTokenExpired(expirationMs = 86400000)

// 移除 Token（登出）
removeToken()

// 获取用户名
setUsername(username)
getUsername()
```

### 2. Axios 拦截器 (`utils/axios.js`)

自动处理请求和响应：

**请求拦截器：**
- 自动添加 `Authorization: Bearer <token>` Header

**响应拦截器：**
- 401 错误：Token 过期，自动清除并跳转登录
- 403 错误：无权访问，显示错误提示
- 网络错误：友好提示

### 3. API 服务 (`services/api.js`)

认证相关方法：

```javascript
// 验证码存储
storeCaptcha(username, captcha)

// 管理员登录
adminLogin(username, password, captcha)

// 管理员注册
registerAdmin(username, password, currentPassword, currentAdminUsername)
```

### 4. 登录页面 (`views/Admin.vue`)

集成 JWT 登录：
- 获取验证码
- 提交登录请求
- 保存 Token 和用户名
- 成功后跳转到管理后台

### 5. 路由守卫 (`router/index.js`)

保护受认证路由：
- `meta.requiresAuth: true` 标记需要认证的路由
- 未登录用户自动重定向到登录页
- 已登录用户访问登录页自动跳转到管理后台

## 使用说明

### 登录流程

1. 用户访问 `/Admin` 或使用 `/ToolsStandalone` 时未认证
2. 输入账号、密码和验证码
3. 调用 `adminLogin()` 获取 JWT Token
4. Token 保存到 localStorage，有效期 24 小时
5. 跳转到管理后台

### 受保护接口访问

所有 API 请求会自动带上 Authorization Header：

```javascript
// 无需手动设置，axios 拦截器会自动添加
await getProducts()
await createComplaint(data)
```

### Token 过期处理

当后端返回 401 错误时：
1. 自动清除 localStorage 中的 Token
2. 显示 "登录已过期" 提示
3. 1.5 秒后重定向到登录页

### 退出登录

在 `Header.vue` 或 `ToolsStandalone.vue` 中调用：

```javascript
removeToken()
router.push('/')
```

## 环境变量

配置 API 基础地址（`.env` 文件）：

```env
VITE_API_BASE_URL=/api
```

## 安全注意事项

1. **Token 存储**：使用 localStorage，注意防范 XSS 攻击
2. **有效期**：Token 有效期 24 小时，超时需重新登录
3. **HTTPS**：生产环境必须使用 HTTPS 传输
4. **验证码**：登录时必须验证验证码，防止暴力破解

## 错误处理

| 错误码 | 说明 | 处理方式 |
|--------|------|----------|
| 401 | Token 无效或过期 | 清除 Token，跳转登录 |
| 403 | 无权访问 | 显示错误提示 |
| 400 | 请求参数错误 | 显示具体错误信息 |
| 500 | 服务器错误 | 显示通用错误提示 |

## 相关文件

- `frontend/src/utils/auth.js` - Token 管理工具
- `frontend/src/utils/axios.js` - Axios 拦截器
- `frontend/src/services/api.js` - API 服务
- `frontend/src/views/Admin.vue` - 登录页面
- `frontend/src/views/ToolsStandalone.vue` - 管理后台
- `frontend/src/components/Header.vue` - 顶部导航
- `frontend/src/router/index.js` - 路由配置

## 测试步骤

1. 访问 `/Admin` 登录页面
2. 输入账号密码和验证码
3. 登录成功后跳转到 `/ToolsStandalone`
4. 尝试访问受保护接口（如 `/getAllComplaintInfo`）
5. 在浏览器控制台查看 Network，确认 Authorization Header 已添加
6. 等待 24 小时或手动修改 localStorage 让 Token 过期
7. 刷新页面，确认自动跳转登录
8. 点击退出登录，确认 Token 清除

## 与后端集成

后端 JWT 实现位于：
- `backend/src/main/java/com/foodtraceability/util/JwtTokenProvider.java`
- `backend/src/main/java/com/foodtraceability/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/foodtraceability/config/WebSecurityConfig.java`

后端配置的 JWT 密钥和有效期必须与前端期望的一致：
- 有效期：86400 秒（24 小时）
- Token 类型：Bearer
