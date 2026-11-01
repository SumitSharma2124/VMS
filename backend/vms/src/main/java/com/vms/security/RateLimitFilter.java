package com.vms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long LIMIT = 20;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/public/")) {
            filterChain.doFilter(request, response);
            return;
        }
        String key = "rate-limit:" + request.getRemoteAddr();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        if (count != null && count > LIMIT) {
            response.setStatus(429);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getOutputStream(), Map.of(
                    "status", 429,
                    "message", "Too many requests"
            ));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
