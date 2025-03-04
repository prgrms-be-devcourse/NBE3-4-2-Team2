package com.example.backend.social.reaction.like.scheduler;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.example.backend.social.reaction.like.dto.LikeInfo;
import com.example.backend.social.reaction.like.service.LikeSyncService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikeSyncManager {
    private final LikeSyncService likeSyncService;
    
    /**
     * 좋아요 정보를 동기화 대기열에 추가
     */
    public void scheduleSyncToDatabase(Long memberId, Long resourceId, String resourceType, boolean isActive, boolean isNewLike) {
        LikeInfo likeInfo = new LikeInfo(
            memberId, 
            resourceId, 
            resourceType, 
            isNewLike ? LocalDateTime.now() : null,
            LocalDateTime.now(),
            isActive
        );
        
        likeSyncService.addToPendingSync(likeInfo);
    }
}
