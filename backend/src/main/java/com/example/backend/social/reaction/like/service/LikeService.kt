package com.example.backend.social.reaction.like.service

import com.example.backend.entity.MemberRepository
import com.example.backend.social.exception.SocialErrorCode
import com.example.backend.social.exception.SocialException
import com.example.backend.social.reaction.like.converter.LikeConverter
import com.example.backend.social.reaction.like.dto.LikeInfo
import com.example.backend.social.reaction.like.dto.LikeToggleResponse
import com.example.backend.social.reaction.like.scheduler.LikeSyncManager
import com.example.backend.social.reaction.like.util.RedisKeyUtil
import com.example.backend.social.reaction.like.util.component.LikeEventPublisher
import com.example.backend.social.reaction.like.util.component.OwnerChecker
import com.example.backend.social.reaction.like.util.component.RedisLikeService
import com.example.backend.social.reaction.like.util.component.ResourceResolver
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 좋아요 서비스
 * 좋아요 서비스 관련 로직 구현
 *
 * @author Metronon
 * @since 2025-03-01
 */
@Service
open class LikeService(
    private val memberRepository: MemberRepository,
    private val ownerChecker: OwnerChecker,
    private val resourceResolver: ResourceResolver,
    private val redisLikeService: RedisLikeService,
    private val likeEventPublisher: LikeEventPublisher,
    private val likeSyncManager: LikeSyncManager
) {

/**
     * 좋아요 토글 메서드
     * 리소스의 타입을 통해 대상 확인 및 좋아요 토글을 진행합니다.
     * 좋아요 취소의 경우 Redis 에 데이터가 없는 경우에만 서버 통신을 진행합니다.
     *
     * @param memberId, resourceType, resourceId
     * @return LikeToggleResponse (DTO)
     */
    @Transactional
    open fun toggleLike(memberId: Long, resourceType: String, resourceId: Long?): LikeToggleResponse {
        // 1. 멤버 id를 통해 멤버 가져오기
        val member = memberRepository!!.findById(memberId)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "로그인 정보 확인에 실패했습니다.") }

        // 2. 타입을 통해 리소스 가져오기
        val resource = resourceResolver!!.resolveResource(resourceType, resourceId)

        // 3. 본인의 컨텐츠인지 확인
        if (ownerChecker!!.isOwner(member, resource)) {
            throw SocialException(SocialErrorCode.CANNOT_PERFORM_ON_SELF, "자신의 컨텐츠에는 좋아요를 할 수 없습니다.")
        }

        // 4. Redis 키 생성 및 리소스 타입 정규화
        val upperResourceType = resourceResolver.normalizeResourceType(resourceType)
        val likeKey = RedisKeyUtil.getLikeKey(upperResourceType, resourceId, memberId)
        val countKey = RedisKeyUtil.getLikeCountKey(upperResourceType, resourceId)

        // 5. 현재 좋아요 상태 확인
        val likeStateInfo = redisLikeService!!.getLikeState(
            likeKey, memberId, resourceId, upperResourceType
        )
        val currentlyLiked = likeStateInfo.currentlyLiked
        val isNewLike = likeStateInfo.isNewLike

        // 6. 상태 토글
        val newLikedState = !currentlyLiked

        // 7. Redis 업데이트
        val likeInfo = LikeInfo(
            memberId,
            resourceId,
            upperResourceType,
            if (isNewLike) LocalDateTime.now() else null,
            LocalDateTime.now(),
            newLikedState
        )

        // Redis에 좋아요 정보 저장 및 카운트 업데이트
        redisLikeService.updateLikeInfo(likeKey, likeInfo)
        redisLikeService.updateLikeCount(countKey, newLikedState)

        // 8. 비동기로 DB 업데이트 스케줄링
        likeSyncManager!!.scheduleSyncToDatabase(memberId, resourceId, upperResourceType, newLikedState, isNewLike)

        // 9. 알림 이벤트 발행
        likeEventPublisher!!.publishLikeEvent(member, resource, resourceId, upperResourceType)

        // 10. 좋아요 수 조회
        val likeCount = redisLikeService.getLikeCount(countKey)

        return LikeConverter.toLikeResponse(likeInfo, likeCount)
    }
}
