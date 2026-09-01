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

    public List<Todo> search(String keyword, String category, String order) {
        return todoMapper.search(keyword, category, order);
    }

    public List<Todo> search(String keyword, String category, String order, LocalDate from, LocalDate to) {
        return todoMapper.searchWithDueDate(keyword, category, order, from, to);
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
        todoMapper.deleteById(id);
        log.info("削除: id={}", id);
    }
}
