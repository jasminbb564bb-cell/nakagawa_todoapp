package com.example.todoapp;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShareMapper {

    TaskShare findByToken(@Param("token") String token);

    TaskShare findActiveByTodoId(@Param("todoId") Long todoId);

    int insert(TaskShare taskShare);

    int deleteExpired();

    int deleteByTodoId(@Param("todoId") Long todoId);
}
