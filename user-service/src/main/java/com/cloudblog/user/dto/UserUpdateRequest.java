package com.cloudblog.user.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String nickname;
    private String signature;
    private String email;
}
