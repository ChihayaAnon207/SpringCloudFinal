package com.cloudblog.blog.dto;

import lombok.Data;

@Data
public class BlogUpdateRequest {
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private Integer status;
}
