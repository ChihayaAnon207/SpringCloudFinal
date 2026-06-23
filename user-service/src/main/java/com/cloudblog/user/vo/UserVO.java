package com.cloudblog.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String signature;
    private String email;
    private LocalDateTime createdAt;
}
