package com.cloudblog.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudblog.blog.dto.BlogCreateRequest;
import com.cloudblog.blog.dto.BlogUpdateRequest;
import com.cloudblog.blog.entity.Blog;
import com.cloudblog.blog.entity.BlogLike;
import com.cloudblog.blog.feign.UserClient;
import com.cloudblog.blog.feign.UserDTO;
import com.cloudblog.blog.mapper.BlogLikeMapper;
import com.cloudblog.blog.mapper.BlogMapper;
import com.cloudblog.blog.service.BlogService;
import com.cloudblog.blog.vo.BlogVO;
import com.cloudblog.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogMapper blogMapper;
    private final BlogLikeMapper blogLikeMapper;
    private final UserClient userClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LIKE_KEY_PREFIX = "blog:likes:";

    @Override
    @Transactional
    public BlogVO createBlog(Long userId, BlogCreateRequest request) {
        Blog blog = new Blog();
        BeanUtils.copyProperties(request, blog);
        blog.setUserId(userId);
        blog.setLikeCount(0);
        if (blog.getStatus() == null) {
            blog.setStatus(1);
        }
        if (StrUtil.isBlank(blog.getSummary()) && StrUtil.isNotBlank(blog.getContent())) {
            blog.setSummary(StrUtil.sub(blog.getContent(), 0, 200));
        }
        blogMapper.insert(blog);
        return toVO(blog, false);
    }

    @Override
    public BlogVO getBlogById(Long id, Long currentUserId) {
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            throw new IllegalArgumentException("Blog not found");
        }
        if (blog.getStatus() == 0 && !blog.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("Blog not found");
        }
        boolean liked = isLiked(id, currentUserId);
        return toVO(blog, liked);
    }

    @Override
    public Page<BlogVO> pageBlogs(Integer page, Integer size, Long userId, Long currentUserId) {
        Page<Blog> blogPage = blogMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Blog>()
                        .eq(Blog::getStatus, 1)
                        .eq(userId != null, Blog::getUserId, userId)
                        .orderByDesc(Blog::getCreatedAt)
        );

        Page<BlogVO> voPage = new Page<>(blogPage.getCurrent(), blogPage.getSize(), blogPage.getTotal());
        List<BlogVO> voList = blogPage.getRecords().stream()
                .map(blog -> toVO(blog, isLiked(blog.getId(), currentUserId)))
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public BlogVO updateBlog(Long userId, Long blogId, BlogUpdateRequest request) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null) {
            throw new IllegalArgumentException("Blog not found");
        }
        if (!blog.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to update this blog");
        }

        if (StrUtil.isNotBlank(request.getTitle())) blog.setTitle(request.getTitle());
        if (StrUtil.isNotBlank(request.getContent())) blog.setContent(request.getContent());
        if (StrUtil.isNotBlank(request.getSummary())) blog.setSummary(request.getSummary());
        if (StrUtil.isNotBlank(request.getCoverImage())) blog.setCoverImage(request.getCoverImage());
        if (request.getStatus() != null) blog.setStatus(request.getStatus());

        blogMapper.updateById(blog);
        return toVO(blog, isLiked(blogId, userId));
    }

    @Override
    @Transactional
    public void deleteBlog(Long userId, Long blogId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null) {
            throw new IllegalArgumentException("Blog not found");
        }
        if (!blog.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to delete this blog");
        }
        blogMapper.deleteById(blogId);
        blogLikeMapper.delete(new LambdaQueryWrapper<BlogLike>().eq(BlogLike::getBlogId, blogId));
        redisTemplate.delete(LIKE_KEY_PREFIX + blogId);
    }

    @Override
    @Transactional
    public Map<String, Object> likeBlog(Long userId, Long blogId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null) {
            throw new IllegalArgumentException("Blog not found");
        }

        String likeKey = LIKE_KEY_PREFIX + blogId;
        String userIdStr = String.valueOf(userId);
        boolean alreadyLiked = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(likeKey, userIdStr));

        if (alreadyLiked) {
            redisTemplate.opsForSet().remove(likeKey, userIdStr);
            blogLikeMapper.delete(new LambdaQueryWrapper<BlogLike>()
                    .eq(BlogLike::getBlogId, blogId)
                    .eq(BlogLike::getUserId, userId));
            blog.setLikeCount(blog.getLikeCount() - 1);
        } else {
            redisTemplate.opsForSet().add(likeKey, userIdStr);
            BlogLike blogLike = new BlogLike();
            blogLike.setBlogId(blogId);
            blogLike.setUserId(userId);
            blogLike.setCreatedAt(LocalDateTime.now());
            blogLikeMapper.insert(blogLike);
            blog.setLikeCount(blog.getLikeCount() + 1);
        }

        blogMapper.updateById(blog);
        return Map.of("liked", !alreadyLiked, "likeCount", blog.getLikeCount());
    }

    private BlogVO toVO(Blog blog, boolean liked) {
        BlogVO vo = new BlogVO();
        BeanUtils.copyProperties(blog, vo);
        vo.setLiked(liked);

        try {
            Result<UserDTO> result = userClient.getUserById(blog.getUserId());
            if (result != null && result.getData() != null) {
                vo.setAuthorNickname(result.getData().getNickname());
                vo.setAuthorAvatar(result.getData().getAvatarUrl());
            }
        } catch (Exception e) {
            vo.setAuthorNickname("Unknown");
        }

        return vo;
    }

    private boolean isLiked(Long blogId, Long userId) {
        if (userId == null) return false;
        String likeKey = LIKE_KEY_PREFIX + blogId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(likeKey, String.valueOf(userId)));
    }
}
