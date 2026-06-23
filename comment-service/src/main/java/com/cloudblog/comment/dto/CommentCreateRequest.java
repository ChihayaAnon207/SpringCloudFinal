package com.cloudblog.comment.dto;

import lombok.Data;

@Data
public class CommentCreateRequest {
    private Long blogId;
    private Long parentId;
    private String content;
}
