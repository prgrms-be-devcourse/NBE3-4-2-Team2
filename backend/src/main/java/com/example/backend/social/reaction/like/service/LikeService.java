package com.example.backend.social.reaction.like.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.CommentRepository;
import com.example.backend.entity.LikeEntity;
import com.example.backend.entity.LikeRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostRepository;
import com.example.backend.global.event.LikeEvent;
import com.example.backend.global.util.RedisKeyUtil;
import com.example.backend.social.exception.SocialErrorCode;
import com.example.backend.social.exception.SocialException;
import com.example.backend.social.reaction.like.converter.LikeConverter;
import com.example.backend.social.reaction.like.dto.LikeInfo;
import com.example.backend.social.reaction.like.dto.LikeToggleResponse;

/**
 * 좋아요 서비스
 * 좋아요 서비스 관련 로직 구현
 *
 * @author Metronon
 * @since 2025-01-30
 */
@Service
public class LikeService {
	private final LikeRepository likeRepository;
	private final MemberRepository memberRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final OwnerChecker ownerChecker;
	private final RedisTemplate<String, Object> redisTemplate;
	private final StringRedisTemplate stringRedisTemplate;
	private final ApplicationEventPublisher applicationEventPublisher;

	private static final Duration CACHE_TTL = Duration.ofDays(7);

	@Autowired
	public LikeService(MemberRepository memberRepository,
		PostRepository postRepository,
		CommentRepository commentRepository,
		LikeRepository likeRepository,
		OwnerChecker ownerChecker,
		RedisTemplate<String, Object> redisTemplate,
		StringRedisTemplate stringRedisTemplate,
		ApplicationEventPublisher applicationEventPublisher) {
		this.memberRepository = memberRepository;
		this.postRepository = postRepository;
		this.commentRepository = commentRepository;
		this.likeRepository = likeRepository;
		this.ownerChecker = ownerChecker;
		this.redisTemplate = redisTemplate;
		this.stringRedisTemplate = stringRedisTemplate;
		this.applicationEventPublisher = applicationEventPublisher;
	}
	/**
	 * 좋아요 토글 메서드
	 * 리소스의 타입을 통해 대상 확인 및 좋아요 토글을 진행합니다.
	 * 좋아요 취소의 경우 Redis에 데이터가 없는 경우에만 서버 통신을 진행합니다.
	 *
	 * @param memberId, resourceType, resourceId
	 * @return LikeToggleResponse (DTO)
	 */
	@Transactional
	public LikeToggleResponse toggleLike(long memberId, String resourceType, Long resourceId) {
		// 1. 멤버 id를 통해 멤버 가져오기
		MemberEntity member = memberRepository.findById(memberId)
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "로그인 정보 확인에 실패했습니다."));

		// 2. 타입을 통해 리소스 가져오기
		Object resource = switch (resourceType) {
			case "post" -> postRepository.findById(resourceId)
				.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "게시물을 찾을 수 없습니다."));
			case "comment", "reply" -> commentRepository.findById(resourceId)
				.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));
			default -> throw new IllegalArgumentException("리소스 타입을 확인할 수 없습니다: " + resourceType);
		};

		// 3. 본인의 컨텐츠인지 확인
		if (ownerChecker.isOwner(member, resource)) {
			throw new SocialException(SocialErrorCode.CANNOT_PERFORM_ON_SELF, "자신의 컨텐츠에는 좋아요를 할 수 없습니다.");
		}

		// 4. Redis 키 생성
		String upperResourceType = resourceType.toUpperCase();
		String likeKey = RedisKeyUtil.getLikeKey(upperResourceType, resourceId, memberId);
		String countKey = RedisKeyUtil.getLikeCountKey(upperResourceType, resourceId);

		// 5. 현재 좋아요 상태 확인
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
			Optional<LikeEntity> likeOp = likeRepository.findByMemberIdAndResourceIdAndResourceType(memberId, resourceId, upperResourceType);
			if (likeOp.isPresent()) {
				currentlyLiked = likeOp.get().isLiked();
			} else {
				isNewLike = true;
			}
		}

		// 6. 상태 토글
		boolean newLikedState = !currentlyLiked;

		// 7. Redis 업데이트
		LikeInfo likeInfo = new LikeInfo(
			memberId,
			resourceId,
			upperResourceType,
			isNewLike ? LocalDateTime.now() : null,
			LocalDateTime.now(),
			newLikedState
		);

		// Redis에 저장
		redisTemplate.opsForValue().set(likeKey, likeInfo);
		redisTemplate.expire(likeKey, CACHE_TTL);

		// 좋아요 수 업데이트
		if (newLikedState) {
			stringRedisTemplate.opsForValue().increment(countKey);
		} else {
			stringRedisTemplate.opsForValue().decrement(countKey);
		}
		// TTL 설정
		stringRedisTemplate.expire(countKey, CACHE_TTL);

		// 8. 비동기로 DB 업데이트 스케줄링 (여기서는 메서드만 호출)
		scheduleSyncToDatabase(memberId, resourceId, upperResourceType, newLikedState, isNewLike);

		// 9. 알림 이벤트 발행
		Long ownerId = ownerChecker.getOwnerIdFromResource(resource);
		applicationEventPublisher.publishEvent(
			LikeEvent.create(member.getUsername(), ownerId, resourceId, upperResourceType)
		);

		// 10. 좋아요 수 조회
		String countStr = stringRedisTemplate.opsForValue().get(countKey);
		Long likeCount;
		if (countStr != null) {
			likeCount = Long.parseLong(countStr);
		} else {
			// Redis에 countKey가 없는 경우, 기본값 설정 또는 DB에서 조회
			likeCount = 0L;
		}

		return LikeConverter.toLikeResponse(likeInfo, likeCount);
	}

	// 비동기로 DB 업데이트 스케줄링 (구현 필요)
	private void scheduleSyncToDatabase(Long memberId, Long resourceId, String resourceType, boolean isActive, boolean isNewLike) {
		// TODO: 실제 비동기 DB 업데이트 로직 구현
	}
}
