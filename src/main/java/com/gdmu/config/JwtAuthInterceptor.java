package com.gdmu.config;

import com.gdmu.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 从Authorization头获取token
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\": 0, \"msg\": \"未提供认证token\", \"data\": null}");
                return false;
            }

            String token = authorizationHeader.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\": 0, \"msg\": \"无效的认证token\", \"data\": null}");
                return false;
            }

            // 将userId设置到请求属性中
            request.setAttribute("userId", userId);
            log.info("JWT认证成功，userId: {}", userId);
            return true;

        } catch (Exception e) {
            log.error("JWT认证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": 0, \"msg\": \"认证失败: " + e.getMessage() + "\", \"data\": null}");
            return false;
        }
    }
}