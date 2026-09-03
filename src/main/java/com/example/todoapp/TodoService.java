package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@Slf4j
public class TodoService {

    private final TodoMapper todoMapper;
    private final UserMapper userMapper;

    public TodoService(TodoMapper todoMapper, UserMapper userMapper) {
        this.todoMapper = todoMapper;
        this.userMapper = userMapper;
    }

    private Long userId(String username) {
        return userMapper.findByUsername(username).getId();
    }

    public boolean isOwner(String username, Long todoId) {
        Todo todo = todoMapper.findById(todoId);
        return todo != null && userId(username).equals(todo.getUserId());
    }

    public List<Todo> searchForUser(String username, String keyword, String category, String order,
            boolean includeCompleted, int limit, int offset, boolean trash) {
        return todoMapper.searchByUserId(userId(username), keyword, category, order, includeCompleted,
                limit, offset, trash);
    }

    public int countForUser(String username, String keyword, String category, boolean includeCompleted,
            boolean trash) {
        return todoMapper.countByUserId(userId(username), keyword, category, includeCompleted, trash);
    }

    public List<Todo> searchForUser(String username, String keyword, String category, String order,
            LocalDate from, LocalDate to) {
        return todoMapper.searchWithDueDateByUserId(userId(username), keyword, category, order, from, to);
    }

    public List<Todo> search(String keyword, String category, String order, boolean includeCompleted,
            int limit, int offset, boolean trash) {
        return todoMapper.search(keyword, category, order, includeCompleted, limit, offset, trash);
    }

    public int count(String keyword, String category, boolean includeCompleted, boolean trash) {
        return todoMapper.count(keyword, category, includeCompleted, trash);
    }

    public int count(String keyword, String category, boolean includeCompleted) {
        return count(keyword, category, includeCompleted, false);
    }

    public List<Todo> search(String keyword, String category, String order, LocalDate from, LocalDate to) {
        return todoMapper.searchWithDueDate(keyword, category, order, from, to);
    }

    public List<Todo> searchForSummary(LocalDate from, LocalDate to) {
        return todoMapper.searchForSummary(from, to);
    }

    public List<Todo> search(String keyword, String category, String order, boolean includeCompleted,
            int limit, int offset) {
        return search(keyword, category, order, includeCompleted, limit, offset, false);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public void create(String username, Todo todo) {
        todo.setUserId(userId(username));
        todoMapper.insert(todo);
        log.info("登録: id={}", todo.getId());
    }

    public void create(Todo todo) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        create(username, todo);
    }

    public void update(Todo todo) {
        todoMapper.update(todo);
        log.info("編集: id={}", todo.getId());
    }

    public void delete(Long id) {
        todoMapper.softDelete(id);
        log.info("削除: id={}", id);
    }

    public void restore(Long id) { todoMapper.restore(id); }

    public void togglePinned(Long id, boolean pinned) { todoMapper.togglePinned(id, pinned); }
}
