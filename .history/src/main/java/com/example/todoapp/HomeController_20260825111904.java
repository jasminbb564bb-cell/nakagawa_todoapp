package com.example.todoapp;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    private final TodoMapper todoMapper;

    public HomeController(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "繧・ｋ縺薙→邂｡逅・);
        return "index";
    }

    @GetMapping("/todos")
    public String todos(Model model) {
        List<Todo> todos = todoMapper.findAll();
        model.addAttribute("todos", todos);
        return "todos";
    }

    @PostMapping("/todos")
    public String save(@ModelAttribute("todo") Todo todo) {
        todoMapper.insert(todo);
        return "redirect:/todos";
    }

    @GetMapping("/todos/new")
    public String create(@ModelAttribute("todo") Todo todo) {
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String confirm(@ModelAttribute("todo") Todo todo) {
        return "create-confirm";
    }

    @PostMapping("/todos/new")
    public String rewrite(@ModelAttribute("todo") Todo todo) {
        return "create";
    }
}
