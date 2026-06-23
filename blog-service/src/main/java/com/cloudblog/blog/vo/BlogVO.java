package com.cloudblog.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogVO {
    private Long id;
    private Long userId;
    private String authorNickname;
    private String authorAvatar;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private Integer likeCount;
    private Boolean liked;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
