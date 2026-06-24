package com.cloudblog.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudblog.common.JwtUtil;
import com.cloudblog.user.dto.LoginRequest;
import com.cloudblog.user.dto.RegisterRequest;
import com.cloudblog.user.dto.UserUpdateRequest;
import com.cloudblog.user.entity.User;
import com.cloudblog.user.mapper.UserMapper;
import com.cloudblog.user.service.UserService;
import com.cloudblog.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    public void register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (existing != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        // 2. 创建用户，密码加密
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StrUtil.isBlank(request.getNickname())
                ? request.getUsername() : request.getNickname());
        user.setEmail(request.getEmail());

        userMapper.insert(user);
    }

    @Override
    public String login(LoginRequest request) {
        // 1. 查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // 3. 生成并返回 JWT
        return JwtUtil.generateToken(user.getId(), jwtSecret, jwtExpiration);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return toVO(user);
    }

    @Override
    public UserVO updateUser(Long id, UserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // 只更新非空字段（部分更新）
        if (StrUtil.isNotBlank(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StrUtil.isNotBlank(request.getSignature())) {
            user.setSignature(request.getSignature());
        }
        if (StrUtil.isNotBlank(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StrUtil.isNotBlank(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        userMapper.updateById(user);
        return toVO(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
