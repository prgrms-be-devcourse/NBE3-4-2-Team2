package com.example.backend.social.follow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.example.backend.entity.FollowEntity;
import com.example.backend.entity.FollowRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.global.event.FollowEvent;
import com.example.backend.social.exception.SocialErrorCode;
import com.example.backend.social.exception.SocialException;
import com.example.backend.social.follow.converter.FollowConverter;
import com.example.backend.social.follow.dto.CreateFollowResponse;
import com.example.backend.social.follow.dto.DeleteFollowResponse;
import com.example.backend.social.follow.dto.FollowResponse;

import jakarta.transaction.Transactional;

/**
 * 팔로우 서비스
 * 팔로우 서비스 관련 로직 구현
 *
 * @author Metronon
 * @since 2025-03-06
 */
@Service
public class FollowService {
	private final MemberRepository memberRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public FollowService(
		MemberRepository memberRepository,
		ApplicationEventPublisher applicationEventPublisher
	) {
		this.memberRepository = memberRepository;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	/**
	 * 팔로우 요청 메서드
	 * sender(followingList add, followingCount ++)
	 * receiver(followerList add, followerCount ++)
	 *
	 * @param senderUsername, receiverUsername
	 * @return FollowResponse (DTO)
	 */
	@Transactional
	public FollowResponse createFollow(String senderUsername, String receiverUsername) {
		// 1. 팔로우 요청측 검증후 엔티티 가져오기
		MemberEntity sender = memberRepository.findByUsername("senderUsername")
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "요청측 회원 검증에 실패했습니다."));

		// 2. 팔로위측 검증후 엔티티 가져오기
		MemberEntity receiver = memberRepository.findByUsername("receiverUsername")
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "응답측 회원 검증에 실패했습니다."));

		// 3. 이미 팔로우가 되어있는지 검증
		boolean alreadyFollowed = sender.getFollowingList().stream()
			.anyMatch(member -> member.getUsername().equals(receiverUsername));

		if (alreadyFollowed) {
			throw new SocialException(SocialErrorCode.ALREADY_EXISTS, "이미 팔로우 상태입니다.");
		}

		// 4. 팔로우 관계 생성 및 팔로우 카운트 증가
		sender.addFollowing(receiver);
		receiver.addFollower(sender);

		// 5. 팔로우 이벤트 발생
		applicationEventPublisher.publishEvent(
			FollowEvent.create(senderUsername, receiver.getId(), sender.getId()));

		return FollowConverter.toResponse(sender, receiver);
	}

	/**
	 * 팔로우 취소 메서드
	 * sender(followingList remove, followingCount --)
	 * receiver(followerList remove, followerCount --)
	 *
	 * @param senderUsername, receiverUsername
	 * @return FollowResponse (DTO)
	 */
	@Transactional
	public FollowResponse deleteFollow(String senderUsername, String receiverUsername) {
		// 1. 팔로우 요청측(취소 요청하는 회원) 검증 후 엔티티 가져오기
		MemberEntity sender = memberRepository.findByUsername(senderUsername)
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "요청측 회원 검증에 실패했습니다."));

		// 2. 팔로위측(팔로우 당한 회원) 검증 후 엔티티 가져오기
		MemberEntity receiver = memberRepository.findByUsername(receiverUsername)
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "응답측 회원 검증에 실패했습니다."));

		// 3. 팔로우 관계 존재 여부 검증
		boolean isFollowing = sender.getFollowingList().stream()
			.anyMatch(member -> member.getUsername().equals(receiverUsername));
		if (!isFollowing) {
			throw new SocialException(SocialErrorCode.NOT_FOUND, "팔로우 관계를 찾을 수 없습니다.");
		}

		// 4. 팔로우 취소 관계 처리 및 팔로우 카운트 감소
		sender.removeFollowing(receiver);
		receiver.removeFollower(sender);

		return FollowConverter.toResponse(sender, receiver);
	}

	@Transactional
	public boolean findMutualFollow(Long currentMemberId, Long memberId) {
		return followRepository.countMutualFollow(currentMemberId, memberId) == 2;
	}
}
