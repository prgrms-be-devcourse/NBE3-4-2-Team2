package com.example.backend.social.reaction.like.util.component;

import static com.example.backend.entity.QCommentEntity.*;
import static com.example.backend.entity.QLikeEntity.*;
import static com.example.backend.entity.QPostEntity.*;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
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

            log.info("좋아요 수 동기화 완료: 게시물 {}회, 댓글{}회",
                    updatedPosts, updatedComments);
        } catch (Exception e) {
            log.error("좋아요 수 동기화 중 에러 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 포스트 엔티티의 좋아요 수를 서브 쿼리를 이용해 동기화합니다.
     * 각 포스트마다 likeEntity 테이블에서 실제 좋아요 카운트를 계산한 후,
     * 현재 저장된 값과 다르면 업데이트합니다.
     *
     * @return 업데이트된 포스트 건수
     */
    private int synchronizePostLikeCounts() {
        // 포스트마다 좋아요 수를 계산하는 서브 쿼리
        NumberExpression<Long> likeCountSubQuery = Expressions.numberTemplate(
            Long.class,
            "({0})",
            JPAExpressions.select(likeEntity.count())
                .from(likeEntity)
                .where(
                    likeEntity.resourceId.eq(postEntity.id),
                    likeEntity.resourceType.eq("POST"),
                    likeEntity.isLiked.isTrue()
                )
        );

        // 삭제되지 않은 포스트 중, 서브 쿼리 결과와 현재 likeCount가 다른 포스트 업데이트
        long updatedPosts = queryFactory
            .update(postEntity)
            .set(postEntity.likeCount, likeCountSubQuery)
            .where(
                postEntity.isDeleted.isFalse(),
                likeCountSubQuery.ne(postEntity.likeCount)
            )
            .execute();

        log.info("게시글 {}개 -> 좋아요 수 동기화 완료", updatedPosts);
        return (int) updatedPosts;
    }

    /**
     * 댓글 엔티티의 좋아요 수를 서브 쿼리를 이용해 동기화합니다.
     * 각 댓글마다 likeEntity 테이블에서 실제 좋아요 카운트를 계산한 후,
     * 현재 저장된 값과 다르면 업데이트합니다.
     *
     * @return 업데이트된 댓글 건수
     */
    private int synchronizeCommentLikeCounts() {
        // 댓글마다 좋아요 수를 계산하는 서브 쿼리
        NumberExpression<Long> likeCountSubQuery = Expressions.numberTemplate(
            Long.class,
            "({0})",
            JPAExpressions.select(likeEntity.count())
                .from(likeEntity)
                .where(
                    likeEntity.resourceId.eq(commentEntity.id),
                    likeEntity.resourceType.eq("COMMENT"),
                    likeEntity.isLiked.isTrue()
                )
        );

        // 삭제되지 않은 댓글 중, 서브 쿼리 결과와 현재 likeCount가 다른 댓글 업데이트
        long updatedComments = queryFactory
            .update(commentEntity)
            .set(commentEntity.likeCount, likeCountSubQuery)
            .where(
                commentEntity.isDeleted.isFalse(),
                likeCountSubQuery.ne(commentEntity.likeCount)
            )
            .execute();

        log.info("댓글/대댓글 {}개 -> 좋아요 수 동기화 완료", updatedComments);
        return (int) updatedComments;
    }
}
