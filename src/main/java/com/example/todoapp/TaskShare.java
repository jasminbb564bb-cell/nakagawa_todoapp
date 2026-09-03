package com.example.todoapp;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TaskShare {

    private Long todoId;
    private String token;
    private Long createdBy;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
