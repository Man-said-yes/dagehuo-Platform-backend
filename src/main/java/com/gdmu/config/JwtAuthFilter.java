package com.gdmu.config;

import com.gdmu.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        log.info("JWT过滤器开始处理请求，uri: {}", request.getRequestURI());
        
        // 跳过登录、测试接口、Swagger相关路径、H2控制台和其他静态资源的认证
        String requestUri = request.getRequestURI();
        if (requestUri.equals("/api/auth/login") || 
            requestUri.equals("/api/auth/test") ||
            requestUri.startsWith("/swagger-ui") ||
            requestUri.startsWith("/v3/api-docs") ||
            requestUri.startsWith("/h2-console") ||
            requestUri.equals("/favicon.ico") ||
            requestUri.equals("/hybridaction/zybTrackerStatisticsAction")) {
            log.info("跳过认证，uri: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        // 从Authorization头获取token
        String authorizationHeader = request.getHeader("Authorization");
        log.info("Authorization头: {}", authorizationHeader);
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("未提供认证token，uri: {}", requestUri);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": 0, \"msg\": \"未提供认证token\", \"data\": null}");
            return;
        }

        try {
            String token = authorizationHeader.substring(7);
            log.info("提取的token: {}", token.substring(0, 20) + "...");
            
            Long userId = jwtUtil.getUserIdFromToken(token);
            log.info("从token中获取的userId: {}", userId);
            
            if (userId == null) {
                log.warn("无效的认证token，uri: {}", requestUri);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\": 0, \"msg\": \"无效的认证token\", \"data\": null}");
                return;
            }

            // 将userId设置到请求属性中
            request.setAttribute("userId", userId);
            log.info("JWT认证成功，userId: {}, uri: {}", userId, requestUri);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT认证失败: {}, uri: {}", e.getMessage(), requestUri);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": 0, \"msg\": \"认证失败\", \"data\": null}");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("JWT过滤器初始化");
    }

    @Override
    public void destroy() {
        log.info("JWT过滤器销毁");
    }
}