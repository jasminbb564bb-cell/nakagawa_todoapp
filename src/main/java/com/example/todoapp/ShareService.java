package com.example.todoapp;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class ShareService {

    private final ShareMapper shareMapper;
    private final UserMapper userMapper;
    private final TodoService todoService;

    public ShareService(ShareMapper shareMapper, UserMapper userMapper, TodoService todoService) {
        this.shareMapper = shareMapper;
        this.userMapper = userMapper;
        this.todoService = todoService;
    }

    @Transactional
    public TaskShare createShare(String username, Long todoId, long hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("hours must be greater than zero");
        }
        if (!todoService.isOwner(username, todoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Todo is not owned by the current user");
        }

        TaskShare activeShare = shareMapper.findActiveByTodoId(todoId);
        if (activeShare != null) {
            return activeShare;
        }

        TaskShare taskShare = new TaskShare();
        taskShare.setTodoId(todoId);
        taskShare.setToken(UUID.randomUUID().toString());
        taskShare.setCreatedBy(userMapper.findByUsername(username).getId());
        taskShare.setExpiresAt(LocalDateTime.now().plusHours(hours));
        shareMapper.insert(taskShare);
        return taskShare;
    }

    @Transactional
    public void stopShare(String username, Long todoId) {
        if (!todoService.isOwner(username, todoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Todo is not owned by the current user");
        }
        shareMapper.deleteByTodoId(todoId);
    }
}
