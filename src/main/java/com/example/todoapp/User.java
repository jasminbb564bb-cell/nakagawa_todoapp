package com.example.todoapp;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class User {

    private Long id;
    private String username;
    private String password;
    private String role;
    private LocalDateTime createdAt;
}
