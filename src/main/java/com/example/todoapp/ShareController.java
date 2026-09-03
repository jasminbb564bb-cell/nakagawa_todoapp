package com.example.todoapp;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class ShareController {

    private final ShareMapper shareMapper;
    private final TodoMapper todoMapper;
    private final TodoService todoService;

    public ShareController(ShareMapper shareMapper, TodoMapper todoMapper, TodoService todoService) {
        this.shareMapper = shareMapper;
        this.todoMapper = todoMapper;
        this.todoService = todoService;
    }

    @GetMapping("/share/{token}")
    public String view(@PathVariable String token, Model model) {
        TaskShare share = shareMapper.findByToken(token);
        if (share == null) {
            return error(model, "共有リンクが見つかりません。");
        }
        if (!share.getExpiresAt().isAfter(LocalDateTime.now())) {
            return error(model, "この共有リンクは期限切れです。");
        }

        Todo todo = todoMapper.findById(share.getTodoId());
        if (todo == null) {
            return error(model, "共有対象のTodoが見つかりません。");
        }

        model.addAttribute("todo", todo);
        model.addAttribute("expiresAt", share.getExpiresAt());
        return "share/view";
    }

    @GetMapping("/todos/{id}/share")
    public String settings(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null || !todoService.isOwner(userDetails.getUsername(), id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Todo is not owned by the current user");
        }
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found");
        }
        model.addAttribute("todo", todo);
        return "share/settings";
    }

    private String error(Model model, String message) {
        model.addAttribute("message", message);
        return "share/error";
    }
}
