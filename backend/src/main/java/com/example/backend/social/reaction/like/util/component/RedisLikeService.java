package com.example.backend.social.reaction.like.util.component;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.backend.entity.LikeEntity;
import com.example.backend.entity.LikeRepository;
import com.example.backend.social.reaction.like.dto.LikeInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisLikeService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final LikeRepository likeRepository;
    private static final Duration CACHE_TTL = Duration.ofDays(7);
    
    public LikeStateInfo getLikeState(String likeKey, Long memberId, Long resourceId, String resourceType) {
        boolean currentlyLiked = false;
        boolean isNewLike = false;
        
        Boolean hasKey = redisTemplate.hasKey(likeKey);
        if (Boolean.TRUE.equals(hasKey)) {
            // Redis에 키가 있는 경우
            LikeInfo likeInfo = (LikeInfo) redisTemplate.opsForValue().get(likeKey);
            if (likeInfo != null) {
                currentlyLiked = likeInfo.isActive();
            }
        } else {
            // Redis에 없는 경우 DB 확인
            Optional<LikeEntity> likeOp = likeRepository.findByMemberIdAndResourceIdAndResourceType(
                memberId, resourceId, resourceType);
            if (likeOp.isPresent()) {
                currentlyLiked = likeOp.get().isLiked();
            } else {
                isNewLike = true;
            }
        }
        
        return new LikeStateInfo(currentlyLiked, isNewLike);
    }
    
    public void updateLikeInfo(String likeKey, LikeInfo likeInfo) {
        redisTemplate.opsForValue().set(likeKey, likeInfo);
        redisTemplate.expire(likeKey, CACHE_TTL);
    }
    
    public void updateLikeCount(String countKey, boolean increment) {
        if (increment) {
            stringRedisTemplate.opsForValue().increment(countKey);
        } else {
            stringRedisTemplate.opsForValue().decrement(countKey);
        }
        stringRedisTemplate.expire(countKey, CACHE_TTL);
    }
    
    public Long getLikeCount(String countKey) {
        String countStr = stringRedisTemplate.opsForValue().get(countKey);
        if (countStr != null) {
            return Long.parseLong(countStr);
        }
        return 0L;
    }

    // LikeStateInfo 정보를 담는 내부 클래스
    @Getter
    @AllArgsConstructor
    public static class LikeStateInfo {
        private final boolean currentlyLiked;
        private final boolean isNewLike;
    }
}
