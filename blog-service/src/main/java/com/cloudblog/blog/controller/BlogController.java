package com.cloudblog.blog.controller;

import com.cloudblog.blog.dto.BlogCreateRequest;
import com.cloudblog.blog.dto.BlogUpdateRequest;
import com.cloudblog.blog.service.BlogService;
import com.cloudblog.blog.vo.BlogVO;
import com.cloudblog.common.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @PostMapping
    public Result<BlogVO> create(@RequestBody BlogCreateRequest request,
                                  @RequestHeader("X-User-Id") Long userId) {
        return Result.success(blogService.createBlog(userId, request));
    }

    @GetMapping("/{id}")
    public Result<BlogVO> getById(@PathVariable Long id,
                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(blogService.getBlogById(id, userId));
    }

    @GetMapping("/page")
    public Result<Page<BlogVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(blogService.pageBlogs(page, size, userId));
    }

    @PutMapping("/{id}")
    public Result<BlogVO> update(@PathVariable Long id,
                                  @RequestBody BlogUpdateRequest request,
                                  @RequestHeader("X-User-Id") Long userId) {
        return Result.success(blogService.updateBlog(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                                @RequestHeader("X-User-Id") Long userId) {
        blogService.deleteBlog(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id,
                                             @RequestHeader("X-User-Id") Long userId) {
        return Result.success(blogService.likeBlog(userId, id));
    }
}
