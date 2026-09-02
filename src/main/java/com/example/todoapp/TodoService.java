package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
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

    public void create(Todo todo) {
        todoMapper.insert(todo);
        log.info("登録: id={}", todo.getId());
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
