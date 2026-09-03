package com.example.todoapp;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ShareCleanupScheduler {

    private final ShareMapper shareMapper;

    public ShareCleanupScheduler(ShareMapper shareMapper) {
        this.shareMapper = shareMapper;
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Tokyo")
    @Transactional
    public void deleteExpiredShares() {
        shareMapper.deleteExpired();
    }
}
