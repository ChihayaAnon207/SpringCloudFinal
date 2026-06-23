package com.cloudblog.comment.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudblog.comment.dto.CommentCreateRequest;
import com.cloudblog.comment.entity.Comment;
import com.cloudblog.comment.feign.UserClient;
import com.cloudblog.comment.feign.UserDTO;
import com.cloudblog.comment.mapper.CommentMapper;
import com.cloudblog.comment.service.CommentService;
import com.cloudblog.comment.vo.CommentVO;
import com.cloudblog.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserClient userClient;

    @Override
    @Transactional
    public CommentVO createComment(Long userId, CommentCreateRequest request) {
        if (StrUtil.isBlank(request.getContent())) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        if (request.getBlogId() == null) {
            throw new IllegalArgumentException("Blog ID is required");
        }

        if (request.getParentId() != null) {
            Comment parent = commentMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new IllegalArgumentException("Parent comment not found");
            }
        }

        Comment comment = new Comment();
        comment.setBlogId(request.getBlogId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        commentMapper.insert(comment);

        return toVO(comment);
    }

    @Override
    public List<CommentVO> getCommentsByBlogId(Long blogId) {
        List<Comment> allComments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getBlogId, blogId)
                        .orderByAsc(Comment::getCreatedAt)
        );

        List<Comment> topLevel = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        Map<Long, List<Comment>> replyMap = allComments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentId));

        return topLevel.stream().map(comment -> {
            CommentVO vo = toVO(comment);
            List<CommentVO> replies = replyMap.getOrDefault(comment.getId(), Collections.emptyList())
                    .stream().map(this::toVO).collect(Collectors.toList());
            vo.setReplies(replies);
            return vo;
        }).collect(Collectors.toList());
    }

    private CommentVO toVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);

        try {
            Result<UserDTO> result = userClient.getUserById(comment.getUserId());
            if (result != null && result.getData() != null) {
                vo.setUserNickname(result.getData().getNickname());
                vo.setUserAvatar(result.getData().getAvatarUrl());
            }
        } catch (Exception e) {
            vo.setUserNickname("Unknown");
        }

        return vo;
    }
}
