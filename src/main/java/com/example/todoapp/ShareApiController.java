package com.example.todoapp;

import java.time.LocalDateTime;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
public class ShareApiController {
    private final ShareService shareService;
    public ShareApiController(ShareService shareService) { this.shareService = shareService; }

    @PostMapping("/api/share/{todoId}")
    public ShareResponse create(@PathVariable Long todoId, @RequestBody ShareRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (request == null || request.hours() == null ||
                !(request.hours() == 1L || request.hours() == 24L || request.hours() == 168L)) {
            throw new IllegalArgumentException("hours must be 1, 24, or 168");
        }
        TaskShare share = shareService.createShare(userDetails.getUsername(), todoId, request.hours());
        return new ShareResponse(share.getToken(), share.getExpiresAt());
    }

    @DeleteMapping("/api/share/{todoId}")
    public void stop(@PathVariable Long todoId, @AuthenticationPrincipal UserDetails userDetails) {
        shareService.stopShare(userDetails.getUsername(), todoId);
    }

    public record ShareRequest(Long hours) {}
    public record ShareResponse(String token, LocalDateTime expiresAt) {}
}
