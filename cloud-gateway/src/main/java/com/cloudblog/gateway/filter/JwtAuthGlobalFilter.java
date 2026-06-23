package com.cloudblog.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.cloudblog.common.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("#{'${jwt.white-list}'.split(',')}")
    private List<String> whiteList;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单直接放行
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        // 从 Header 获取 Token
        String token = extractToken(request);
        if (StrUtil.isBlank(token)) {
            return unauthorized(exchange.getResponse(), "Missing token");
        }

        // 验证 Token
        try {
            Long userId = JwtUtil.getUserIdFromToken(token, jwtSecret);
            // 将 userId 放入请求头，传递给下游服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            return unauthorized(exchange.getResponse(), "Invalid or expired token");
        }
    }

    private boolean isWhiteListed(String path) {
        for (String pattern : whiteList) {
            Pattern compiled = Pattern.compile(pattern.trim());
            if (compiled.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String msg) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] bytes = ("{\"code\":401,\"msg\":\"" + msg + "\",\"data\":null}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
