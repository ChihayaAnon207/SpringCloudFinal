package com.cloudblog.user.service;

import com.cloudblog.user.dto.LoginRequest;
import com.cloudblog.user.dto.RegisterRequest;
import com.cloudblog.user.dto.UserUpdateRequest;
import com.cloudblog.user.vo.UserVO;

public interface UserService {

    void register(RegisterRequest request);

    String login(LoginRequest request);

    UserVO getUserById(Long id);

    UserVO updateUser(Long id, UserUpdateRequest request);
}
