package com.foodtraceability.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 开启方法级安全注解，方便细粒度控制
public class WebSecurityConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    /**
     * 提前定义密码编码器，防止后续引入认证组件时因缺少Bean而报错。
     * 同时为负责用户管理的服务提供统一的密码加密/验证工具。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链：无状态、无CSRF、JWT认证、精细化授权、JSON异常响应。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            // 1. 无状态API，关闭CSRF
            .csrf(csrf -> csrf.disable())
            // 2. 应用自定义CORS配置
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 3. 不创建会话
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 4. 请求授权规则（按需调整）
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()   // 健康检查公开
                .requestMatchers("/api/public/**").permitAll()                      // 假设的公开API
                .requestMatchers("/api/auth/**").permitAll()                        // 登录注册接口公开
                .requestMatchers("/api/**").authenticated()                        // 其他API需要认证
                .anyRequest().denyAll()                                            // 其余一律拒绝
            )
            // 5. 添加JWT过滤器（在用户名密码过滤器之前）
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            // 6. 禁用表单登录和HTTP Basic
            .formLogin(form -> form.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            // 7. 自定义认证/授权异常处理（返回JSON）
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"未认证，请提供有效令牌\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"error\":\"权限不足\"}");
                })
            )
            // 8. 安全头设置
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())           // 防止点击劫持
                .xssProtection(xss -> xss.disable())           // 现代浏览器已不使用，但可显式关闭让前端CSP接管
                .contentTypeOptions(contentType -> contentType.disable()) // 默认启用，可按需配置
            );

        return http.build();
    }

    /**
     * CORS配置：自动处理通配符与allowCredentials的兼容问题。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if ("*".equals(allowedOrigins)) {
            // 使用模式匹配处理通配符，避免与allowCredentials冲突
            configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        } else {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Content-Disposition"));
        configuration.setAllowCredentials(true);  // 允许携带Cookie/Authorization头
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * JWT认证过滤器（需要你根据实际JWT库实现）。
     * 这里仅声明为Bean，由Spring注入到过滤链中。
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}