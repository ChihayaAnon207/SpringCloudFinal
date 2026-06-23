package com.cloudblog.comment.controller;

import com.cloudblog.comment.dto.CommentCreateRequest;
import com.cloudblog.comment.service.CommentService;
import com.cloudblog.comment.vo.CommentVO;
import com.cloudblog.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public Result<CommentVO> create(@RequestBody CommentCreateRequest request,
                                     @RequestHeader("X-User-Id") Long userId) {
        return Result.success(commentService.createComment(userId, request));
    }

    @GetMapping("/blog/{blogId}")
    public Result<List<CommentVO>> getByBlogId(@PathVariable Long blogId) {
        return Result.success(commentService.getCommentsByBlogId(blogId));
    }
}
