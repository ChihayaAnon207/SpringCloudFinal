package com.cloudblog.comment.feign;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String nickname;
    private String avatarUrl;
}
