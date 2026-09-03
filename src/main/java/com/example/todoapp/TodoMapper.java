package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {

    List<Todo> search(@Param("keyword") String keyword, @Param("category") String category,
            @Param("order") String order, @Param("includeCompleted") boolean includeCompleted,
            @Param("limit") int limit, @Param("offset") int offset, @Param("trash") boolean trash);

    List<Todo> searchByUserId(@Param("userId") Long userId, @Param("keyword") String keyword,
            @Param("category") String category, @Param("order") String order,
            @Param("includeCompleted") boolean includeCompleted, @Param("limit") int limit,
            @Param("offset") int offset, @Param("trash") boolean trash);

    int countByUserId(@Param("userId") Long userId, @Param("keyword") String keyword,
            @Param("category") String category, @Param("includeCompleted") boolean includeCompleted,
            @Param("trash") boolean trash);

    int count(@Param("keyword") String keyword, @Param("category") String category,
            @Param("includeCompleted") boolean includeCompleted, @Param("trash") boolean trash);

    List<Todo> searchWithDueDate(@Param("keyword") String keyword, @Param("category") String category,
            @Param("order") String order, @Param("from") LocalDate from, @Param("to") LocalDate to);

    List<Todo> searchWithDueDateByUserId(@Param("userId") Long userId, @Param("keyword") String keyword,
            @Param("category") String category, @Param("order") String order,
            @Param("from") LocalDate from, @Param("to") LocalDate to);

    List<Todo> searchForSummary(@Param("from") LocalDate from, @Param("to") LocalDate to);

    Todo findById(Long id);

    int insert(Todo todo);

    int update(Todo todo);

    int deleteById(Long id);
    int softDelete(Long id);
    int restore(Long id);
    int togglePinned(@Param("id") Long id, @Param("pinned") boolean pinned);
}
