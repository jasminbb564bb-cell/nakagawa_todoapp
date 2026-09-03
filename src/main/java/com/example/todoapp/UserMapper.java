package com.example.todoapp;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("""
            SELECT id, username, password, role
            FROM users
            WHERE username = #{username}
            """)
    User findByUsername(String username);
}
