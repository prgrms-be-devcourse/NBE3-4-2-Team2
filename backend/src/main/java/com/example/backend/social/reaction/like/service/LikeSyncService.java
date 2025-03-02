package com.example.backend.social.reaction.like.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.LikeRepositoryCustomImpl;
import com.example.backend.social.reaction.like.dto.LikeInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeSyncService {
    private final LikeRepositoryCustomImpl likeRepositoryCustom;
    private final RedisTemplate<String, Object> redisTemplate;

    // 동기화 대기 중인 좋아요 목록을 저장할 큐
    private final Queue<LikeInfo> pendingLikes = new ConcurrentLinkedQueue<>();
    
    // 마지막 동기화 시간
    private AtomicLong lastSyncTime = new AtomicLong(System.currentTimeMillis());
    
    // 임계값 설정
    private static final int BATCH_SIZE_THRESHOLD = 5;
    private static final long TIME_THRESHOLD_MS = 30_000; // 30초
    
    /**
     * 좋아요 동기화 큐에 추가
     */
    public void addToPendingSync(LikeInfo likeInfo) {
        pendingLikes.add(likeInfo);
        
        // 임계값 체크 후 필요시 동기화 실행
        if (shouldSync()) {
            syncToDatabase();
        }
    }
    
    /**
     * 동기화가 필요한지 체크
     */
    private boolean shouldSync() {
        return pendingLikes.size() >= BATCH_SIZE_THRESHOLD || 
               (System.currentTimeMillis() - lastSyncTime.get() >= TIME_THRESHOLD_MS);
    }
    
    /**
     * 데이터베이스 동기화 실행
     */
    @Transactional
    public void syncToDatabase() {
        if (pendingLikes.isEmpty()) {
            return;
        }
        
        log.info("Syncing {} likes to database", pendingLikes.size());
        
        // 큐에서 처리할 좋아요 목록 추출
        List<LikeInfo> likesToSync = new ArrayList<>();
        LikeInfo like;
        while ((like = pendingLikes.poll()) != null) {
            likesToSync.add(like);
        }
        
        try {
            // QueryDSL을 사용한 벌크 업데이트 실행
            int updatedCount = likeRepositoryCustom.bulkUpsertLikes(likesToSync);
            log.info("Successfully synced {} likes to database", updatedCount);
        } catch (Exception e) {
            // 에러 발생 시 큐에 다시 추가
            log.error("Failed to sync likes to database", e);
            pendingLikes.addAll(likesToSync);
        }
        
        // 마지막 동기화 시간 업데이트
        lastSyncTime.set(System.currentTimeMillis());
    }
}
