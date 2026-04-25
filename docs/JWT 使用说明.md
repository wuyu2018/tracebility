# JWT 认证使用说明

## 快速开始

### 1. 登录获取 Token

```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123", "captcha": "12345"}'
```

响应：
```json
{
  "username": "admin",
  "token": "<JWT_TOKEN>",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

### 2. 使用 Token 访问 API

```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

## 前端集成

### 保存 Token
```javascript
localStorage.setItem('token', response.data.token)
```

### 发送请求时添加 Token
```javascript
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

### 处理过期
```javascript
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      // 跳转登录页
    }
    return Promise.reject(error)
  }
)
```

## 配置说明

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| JWT_SECRET | 签名密钥（至少 32 字符） | 默认密钥 |
| JWT_EXPIRATION_MS | Token 有效期（毫秒） | 86400000 (24 小时) |

## 安全建议

- ✅ 生产环境修改 JWT_SECRET
- ✅ 设置 HTTPS
- ✅ 限制 CORS 域名
- ❌ 不要提交密钥到 Git

---

**更新日期**: 2026-04-25
