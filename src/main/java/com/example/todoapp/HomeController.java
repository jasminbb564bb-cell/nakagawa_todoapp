package com.example.todoapp;

import java.util.List;
import java.nio.charset.StandardCharsets;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class HomeController {

    private final TodoService todoService;

    public HomeController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "アプリ一覧へ戻る");
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/todos")
    public String todos(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "category", defaultValue = "すべて") String category,
            @RequestParam(name = "order", defaultValue = "asc") String order,
            @RequestParam(name = "includeCompleted", defaultValue = "false") boolean includeCompleted,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "trash", defaultValue = "0") int trash,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        String username = userDetails.getUsername();
        String normalizedOrder = "desc".equals(order) ? "desc" : "asc";
        int pageSize = 10;
        boolean showTrash = trash == 1;
        int totalPages = Math.max(1, (todoService.countForUser(username, keyword, category, includeCompleted, showTrash) + pageSize - 1) / pageSize);
        int currentPage = Math.max(1, Math.min(page, totalPages));
        List<Todo> todos = todoService.searchForUser(username, keyword, category, normalizedOrder, includeCompleted,
                pageSize, (currentPage - 1) * pageSize, showTrash);
        model.addAttribute("todos", todos);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("order", normalizedOrder);
        model.addAttribute("includeCompleted", includeCompleted);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("trash", showTrash);
        return "todos";
    }

    @GetMapping(value = "/api/todos.csv", produces = "text/csv")
    public ResponseEntity<byte[]> todosCsv(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "category", defaultValue = "すべて") String category,
            @RequestParam(name = "order", defaultValue = "asc") String order,
            @RequestParam(name = "includeCompleted", defaultValue = "false") boolean includeCompleted,
            @RequestParam(name = "trash", defaultValue = "0") int trash) {
        String normalizedOrder = "desc".equals(order) ? "desc" : "asc";
        List<Todo> todos = todoService.search(keyword, category, normalizedOrder, includeCompleted,
                Integer.MAX_VALUE, 0, trash == 1);

        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("やること,メモ,ジャンル,優先度,期限,状態\r\n");
        for (Todo todo : todos) {
            csv.append(csvCell(todo.getTitle())).append(',')
                    .append(csvCell(todo.getDetail())).append(',')
                    .append(csvCell(todo.getCategory())).append(',')
                    .append(csvCell(priorityLabel(todo.getPriority()))).append(',')
                    .append(csvCell(todo.getDueDate() == null ? "" : todo.getDueDate().toString())).append(',')
                    .append(csvCell(Boolean.TRUE.equals(todo.getCompleted()) ? "完了" : "未完了"))
                    .append("\r\n");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"todos.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String priorityLabel(Integer priority) {
        return priority == null ? "" : priority == 1 ? "高" : priority == 2 ? "中" : "低";
    }

    private String csvCell(String value) {
        if (value == null) return "";
        String safe = value.startsWith("=") || value.startsWith("+") ? "'" + value : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    @PostMapping("/todos")
    public String save(@ModelAttribute("todo") Todo todo,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        todoService.create(userDetails.getUsername(), todo);
        redirectAttributes.addFlashAttribute("message", "登録しました");
        return "redirect:/todos";
    }

    @GetMapping("/todos/new")
    public String create(@ModelAttribute("todo") Todo todo) {
        return "create";
    }

    @GetMapping("/todos/{id}/duplicate")
    public String duplicate(@PathVariable("id") Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        requireOwner(userDetails, id);
        Todo source = todoService.findById(id);
        if (source == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found");
        }
        Todo copy = new Todo();
        copy.setTitle(source.getTitle());
        copy.setDetail(source.getDetail());
        copy.setCategory(source.getCategory());
        copy.setPriority(source.getPriority());
        copy.setDueDate(source.getDueDate());
        model.addAttribute("todo", copy);
        model.addAttribute("duplicateMode", true);
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String confirm(@Valid @ModelAttribute("todo") Todo todo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "create";
        }
        return "create-confirm";
    }

    @PostMapping("/todos/new")
    public String rewrite(@ModelAttribute("todo") Todo todo) {
        return "create";
    }

    @GetMapping("/todos/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal UserDetails userDetails) {
        requireOwner(userDetails, id);
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "edit";
    }

    @PostMapping("/todos/{id}/confirm")
    public String editConfirm(@PathVariable("id") Long id, @Valid @ModelAttribute("todo") Todo todo,
            BindingResult bindingResult, Model model) {
        model.addAttribute("id", id);
        if (bindingResult.hasErrors()) {
            return "edit";
        }
        return "edit-confirm";
    }

    @PostMapping("/todos/{id}/edit")
    public String editRewrite(@PathVariable("id") Long id, @ModelAttribute("todo") Todo todo, Model model) {
        model.addAttribute("id", id);
        return "edit";
    }

    @GetMapping("/todos/{id}/delete")
    public String deleteConfirm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal UserDetails userDetails) {
        requireOwner(userDetails, id);
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "delete";
    }

    @PostMapping("/todos/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal UserDetails userDetails) {
        requireOwner(userDetails, id);
        todoService.delete(id);
        redirectAttributes.addFlashAttribute("message", "削除しました");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/restore")
    public String restore(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        todoService.restore(id);
        return "redirect:/todos?trash=1";
    }

    @PostMapping("/todos/{id}/pin")
    public String togglePin(@PathVariable("id") Long id, @RequestParam boolean pinned,
            @RequestParam(defaultValue = "") String keyword, @RequestParam(defaultValue = "すべて") String category,
            @RequestParam(defaultValue = "asc") String order, @RequestParam(defaultValue = "false") boolean includeCompleted,
            @RequestParam(defaultValue = "0") int trash, @RequestParam(defaultValue = "1") int page) {
        todoService.togglePinned(id, pinned);
        return "redirect:/todos?keyword=" + keyword + "&category=" + category + "&order=" + order
                + "&includeCompleted=" + includeCompleted + "&trash=" + trash + "&page=" + page;
    }

    @PostMapping("/todos/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute("todo") Todo todo,
            RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetails userDetails) {
        requireOwner(userDetails, id);
        todo.setId(id);
        todoService.update(todo);
        redirectAttributes.addFlashAttribute("message", "保存しました");
        return "redirect:/todos";
    }

    private void requireOwner(UserDetails userDetails, Long id) {
        if (!todoService.isOwner(userDetails.getUsername(), id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Todo is not owned by the current user");
        }
    }
}
