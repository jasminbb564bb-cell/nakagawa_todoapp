package com.example.todoapp.api;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.URI;
import java.time.LocalDate;

import com.example.todoapp.Todo;
import com.example.todoapp.TodoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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
            @RequestParam(name = "order", defaultValue = "asc") String order,
            @RequestParam(name = "from", required = false) LocalDate from,
            @RequestParam(name = "to", required = false) LocalDate to,
            @AuthenticationPrincipal UserDetails userDetails) {
        String normalizedOrder = "desc".equals(order) ? "desc" : "asc";
        return todoService.searchForUser(userDetails.getUsername(), keyword, category, normalizedOrder, from, to)
                .stream()
                .map(TodoDto::from)
                .toList();
    }

    @GetMapping("/api/todos/{id}")
    public ResponseEntity<?> todo(@PathVariable Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return notFound(id);
        }
        return ResponseEntity.ok(TodoDto.from(todo));
    }

    @PostMapping("/api/todos")
    public ResponseEntity<TodoDto> create(@Valid @RequestBody TodoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Todo todo = toTodo(request);
        todoService.create(userDetails.getUsername(), todo);
        Todo created = todoService.findById(todo.getId());
        return ResponseEntity.created(URI.create("/api/todos/" + todo.getId()))
                .body(TodoDto.from(created));
    }

    @PutMapping("/api/todos/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TodoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        requireOwner(userDetails, id);
        Todo existing = todoService.findById(id);
        if (existing == null) {
            return notFound(id);
        }
        Todo todo = toTodo(request);
        todo.setId(id);
        todoService.update(todo);
        return ResponseEntity.ok(TodoDto.from(todoService.findById(id)));
    }

    @DeleteMapping("/api/todos/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        requireOwner(userDetails, id);
        Todo existing = todoService.findById(id);
        if (existing == null) {
            return notFound(id);
        }
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void requireOwner(UserDetails userDetails, Long id) {
        if (!todoService.isOwner(userDetails.getUsername(), id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Todo is not owned by the current user");
        }
    }

    private ResponseEntity<org.springframework.http.ProblemDetail> notFound(Long id) {
        org.springframework.http.ProblemDetail problem = org.springframework.http.ProblemDetail.forStatus(404);
        problem.setTitle("Todo not found");
        problem.setDetail("Todo with id " + id + " was not found.");
        problem.setInstance(URI.create("/api/todos/" + id));
        return ResponseEntity.status(404).body(problem);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<org.springframework.http.ProblemDetail> badRequest(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        org.springframework.http.ProblemDetail problem =
                org.springframework.http.ProblemDetail.forStatus(400);
        problem.setTitle("Bad Request");
        problem.setDetail("入力に誤りがあります");
        problem.setInstance(URI.create(request.getRequestURI()));

        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.add(Map.of("field", fieldError.getField(), "message", fieldError.getDefaultMessage()));
        }
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    private Todo toTodo(TodoRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setDetail(request.getDetail());
        todo.setCategory(request.getCategory());
        todo.setPriority(request.getPriority());
        todo.setDueDate(request.getDueDate());
        todo.setCompleted(request.getCompleted());
        return todo;
    }
}
