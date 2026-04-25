# JWT 认证实现说明

## 实现概览

本次 JWT 认证实现基于 Spring Security 和 jjwt 库，为食品溯源系统管理后台提供了完整的认证机制。

## 技术栈

- **Spring Security 6.x** - 安全框架
- **JJWT (Java JWT) 0.11.5** - JWT 库
- **Redis** - 验证码和 Token 黑名单存储
- **HMAC-SHA256** - 签名算法

## 架构设计

```
客户端请求
  │
  ▼
[JWT Authentication Filter] ◄───┐
  │                              │
  ├─── 无 token ───────────────► [公开接口]
  │                              │
  ▼
[验证 token 有效性]
  │
  ├─── 有效 ──────────────────► [设置 SecurityContext] ──► [受保护接口]
  │
  └─── 无效 ──────────────────► [返回 401/403]
```

## 核心组件

### 1. JwtTokenProvider
位置：`util/JwtTokenProvider.java`

职责：
- 生成 JWT Token
- 解析 Token 获取用户名
- 验证 Token 有效性

关键方法：
```java
String generateTokenByUsername(String username)
String getUsernameFromToken(String token)
boolean validateToken(String token)
```

### 2. JwtAuthenticationFilter
位置：`security/JwtAuthenticationFilter.java`

职责：
- 拦截所有 HTTP 请求
- 提取 Authorization Header 中的 Token
- 验证 Token 并设置用户上下文

过滤流程：
1. 从 `Authorization: Bearer <token>` 提取 token
2. 验证 token 有效性
3. 从 token 中解析用户名
4. 创建 `UsernamePasswordAuthenticationToken`
5. 设置到 `SecurityContextHolder`

### 3. WebSecurityConfig
位置：`config/WebSecurityConfig.java`

关键配置：
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/captcha", "/api/login", "/api/admin/register")
                .permitAll()
            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll()
        )
        .addFilterBefore(jwtAuthenticationFilter, 
                        UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

### 4. AdminServiceImpl
位置：`service/impl/AdminServiceImpl.java`

登录流程：
```java
public LoginResponseDTO login(AdminLoginDTO loginDTO) {
    // 1. 验证验证码
    // 2. 验证账号密码
    // 3. 生成 JWT Token
    // 4. 返回 Token 和用户信息
    String token = jwtTokenProvider.generateTokenByUsername(username);
    return new LoginResponseDTO(username, token, "Bearer", expiresIn);
}
```

## API 测试

### 登录接口

```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "captcha": "12345"
  }'
```

### 受保护接口

```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 配置文件

###环境变量

```bash
# .env 文件
JWT_SECRET=your-very-long-and-secure-secret-key
JWT_EXPIRATION_MS=86400000
```

---

**实现日期**: 2026-04-25  
**版本**: 1.0
