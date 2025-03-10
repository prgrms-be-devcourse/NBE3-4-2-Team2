package com.example.backend.social.reaction.like.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.backend.social.reaction.like.service.LikeSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeSyncScheduler {
    private final LikeSyncService likeSyncService;
    
    /**
     * 30초마다 자동으로 좋아요 동기화 실행
     */
    @Scheduled(fixedDelay = 30_000) // 30초마다 실행
    public void scheduledSync() {
        log.debug("좋아요 동기화가 시작됨");
        likeSyncService.syncToDatabase();
    }
}
