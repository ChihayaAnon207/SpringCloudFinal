package com.cloudblog.user.controller;

import com.cloudblog.common.JwtUtil;
import com.cloudblog.common.Result;
import com.cloudblog.user.dto.LoginRequest;
import com.cloudblog.user.dto.RegisterRequest;
import com.cloudblog.user.dto.UserUpdateRequest;
import com.cloudblog.user.service.UserService;
import com.cloudblog.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        Long userId = JwtUtil.getUserIdFromToken(token, jwtSecret);
        UserVO user = userService.getUserById(userId);
        return Result.success(Map.of("token", token, "user", user));
    }

    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestBody UserUpdateRequest request,
                                         @RequestHeader("X-User-Id") Long userId) {
        return Result.success(userService.updateUser(userId, request));
    }
}
