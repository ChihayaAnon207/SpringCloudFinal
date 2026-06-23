package com.cloudblog.blog.service;

import com.cloudblog.blog.dto.BlogCreateRequest;
import com.cloudblog.blog.dto.BlogUpdateRequest;
import com.cloudblog.blog.vo.BlogVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

public interface BlogService {
    BlogVO createBlog(Long userId, BlogCreateRequest request);
    BlogVO getBlogById(Long id, Long currentUserId);
    Page<BlogVO> pageBlogs(Integer page, Integer size, Long currentUserId);
    BlogVO updateBlog(Long userId, Long blogId, BlogUpdateRequest request);
    void deleteBlog(Long userId, Long blogId);
    Map<String, Object> likeBlog(Long userId, Long blogId);
}
