package com.example.todoapp.api;

import java.util.List;

import com.example.todoapp.TodoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoApiController {

    private final TodoService todoService;

    public TodoApiController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/api/todos")
    public List<TodoDto> todos(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "order", defaultValue = "asc") String order) {
        String normalizedOrder = "desc".equals(order) ? "desc" : "asc";
        return todoService.search(keyword, category, normalizedOrder)
                .stream()
                .map(TodoDto::from)
                .toList();
    }
}
