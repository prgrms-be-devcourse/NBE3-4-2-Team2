package com.example.backend.social.reaction.like.util.component;

import static com.example.backend.entity.QLikeEntity.*;
import static com.example.backend.entity.QPostEntity.*;
import static com.example.backend.entity.QCommentEntity.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeCountSynchronizer {

    private final JPAQueryFactory queryFactory;

    /**
     * 30초마다 Post 엔티티와 Comment 엔티티의 likeCount를 동기화
     */
    @Async
    @Scheduled(fixedRate = 30000) // 30초마다 실행
    @Transactional
    public void synchronizeLikeCounts() {
        log.info("========좋아요 동기화 시작========");
        
        try {
            // Post 엔티티 likeCount 동기화
            int updatedPosts = synchronizePostLikeCounts();
            
            // Comment 엔티티 likeCount 동기화
            int updatedComments = synchronizeCommentLikeCounts();
            
            log.info("좋아요 카운트 동기화 완료: 게시물 {}회, 댓글{}회",
                    updatedPosts, updatedComments);
        } catch (Exception e) {
            log.error("좋아요 동기화 중 에러 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * Post 엔티티의 likeCount를 동기화하는 메소드
     * @return updatedCount(post)
     */
    private int synchronizePostLikeCounts() {
        int updatedCount = 0;
        
        // 각 포스트별 실제 좋아요 수를 조회
        var postLikeCounts = queryFactory
                .select(likeEntity.resourceId, likeEntity.count())
                .from(likeEntity)
                .where(
                        likeEntity.resourceType.eq("POST"),
                        likeEntity.isLiked.isTrue()
                )
                .groupBy(likeEntity.resourceId)
                .fetch();
        
        // 좋아요가 있는 포스트들의 ID 목록
        List<Long> postsWithLikes = postLikeCounts.stream()
                .map(tuple -> tuple.get(0, Long.class))
                .collect(Collectors.toList());
        
        // 1. 좋아요가 있지만 카운트가 불일치하는 포스트 업데이트
        for (var postLikeCount : postLikeCounts) {
            Long postId = postLikeCount.get(0, Long.class);
            Long likeCount = postLikeCount.get(1, Long.class);
            
            long updated = queryFactory
                    .update(postEntity)
                    .set(postEntity.likeCount, likeCount)
                    .where(
                            postEntity.id.eq(postId),
                            postEntity.likeCount.ne(likeCount),
                            postEntity.isDeleted.isFalse()
                    )
                    .execute();
            
            updatedCount += updated;
        }
        
        // 2. 좋아요가 0이어야 하지만 현재 0이 아닌 포스트만 선택적으로 업데이트
        if (!postsWithLikes.isEmpty()) {
            long zeroLikeUpdated = queryFactory
                    .update(postEntity)
                    .set(postEntity.likeCount, 0L)
                    .where(
                            postEntity.id.notIn(postsWithLikes),
                            postEntity.likeCount.ne(0L),
                            postEntity.isDeleted.isFalse()
                    )
                    .execute();
            
            updatedCount += zeroLikeUpdated;
        }
        
        log.info("{}개의 게시글 좋아요 카운트 업데이트 완료", updatedCount);
        return updatedCount;
    }

    /**
     * Comment 엔티티의 likeCount를 동기화하는 메소드
     * @return updatedCount(comment)
     */
    private int synchronizeCommentLikeCounts() {
        int updatedCount = 0;
        
        // 각 댓글별 실제 좋아요 수를 조회
        var commentLikeCounts = queryFactory
                .select(likeEntity.resourceId, likeEntity.count())
                .from(likeEntity)
                .where(
                        likeEntity.resourceType.eq("COMMENT"),
                        likeEntity.isLiked.isTrue()
                )
                .groupBy(likeEntity.resourceId)
                .fetch();
        
        // 좋아요가 있는 댓글들의 ID 목록
        List<Long> commentsWithLikes = commentLikeCounts.stream()
                .map(tuple -> tuple.get(0, Long.class))
                .collect(Collectors.toList());
        
        // 1. 좋아요가 있지만 카운트가 불일치하는 댓글 업데이트
        for (var commentLikeCount : commentLikeCounts) {
            Long commentId = commentLikeCount.get(0, Long.class);
            Long likeCount = commentLikeCount.get(1, Long.class);
            
            long updated = queryFactory
                    .update(commentEntity)
                    .set(commentEntity.likeCount, likeCount)
                    .where(
                            commentEntity.id.eq(commentId),
                            commentEntity.likeCount.ne(likeCount),
                            commentEntity.isDeleted.isFalse()
                    )
                    .execute();
            
            updatedCount += updated;
        }
        
        // 2. 좋아요가 0이어야 하지만 현재 0이 아닌 댓글만 선택적으로 업데이트
        if (!commentsWithLikes.isEmpty()) {
            long zeroLikeUpdated = queryFactory
                    .update(commentEntity)
                    .set(commentEntity.likeCount, 0L)
                    .where(
                            commentEntity.id.notIn(commentsWithLikes),
                            commentEntity.likeCount.ne(0L),
                            commentEntity.isDeleted.isFalse()
                    )
                    .execute();
            
            updatedCount += zeroLikeUpdated;
        }
        
        log.info("{}개의 댓글 좋아요 카운트 업데이트 완료", updatedCount);
        return updatedCount;
    }
}
