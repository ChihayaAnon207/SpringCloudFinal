package com.cloudblog.comment.service;

import com.cloudblog.comment.dto.CommentCreateRequest;
import com.cloudblog.comment.vo.CommentVO;

import java.util.List;

public interface CommentService {
    CommentVO createComment(Long userId, CommentCreateRequest request);
    List<CommentVO> getCommentsByBlogId(Long blogId);
}
