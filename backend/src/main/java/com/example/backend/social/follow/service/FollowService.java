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

import jakarta.transaction.Transactional;

/**
 * 팔로우 서비스
 * 팔로우 서비스 관련 로직 구현
 *
 * @author Metronon
 * @since 2025-02-07
 */
@Service
public class FollowService {
	private final FollowRepository followRepository;
	private final MemberRepository memberRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public FollowService(
		FollowRepository followRepository, MemberRepository memberRepository,
		ApplicationEventPublisher applicationEventPublisher
	) {
		this.followRepository = followRepository;
		this.memberRepository = memberRepository;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	/**
	 * 팔로우 요청 메서드
	 * senderId(memberId1)와 receiverId(memberId2)를 받아 followEntity 생성
	 * 각 멤버의 followingCount, followeeCount 증가
	 *
	 * @param senderId, receiverId
	 * @return CreateFollowResponse (DTO)
	 */
	@Transactional
	public CreateFollowResponse createFollow(Long senderId, Long receiverId) {
		// 1. 팔로우 요청측 검증후 엔티티 가져오기
		MemberEntity sender = memberRepository.findById(senderId)
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "요청측 클라이언트 검증에 실패했습니다."));

		// 2. 팔로위측 검증후 엔티티 가져오기
		MemberEntity receiver = memberRepository.findById(receiverId)
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "응답측 클라이언트 검증에 실패했습니다."));

		// 3. 이미 팔로우가 되어있는지 검증
		if (followRepository.existsBySenderIdAndReceiverId(senderId, receiverId)) {
			throw new SocialException(SocialErrorCode.ALREADY_EXISTS);
		}

		// 4. id 및 요청 날짜 포함을 위해 엔티티 생성
		FollowEntity follow = FollowEntity.create(sender, receiver);

		// 5. 팔로우 요청 및 팔로워, 팔로위 인원수 증가 반영
		memberRepository.incrementFollowerCount(senderId);
		memberRepository.incrementFolloweeCount(receiverId);
		followRepository.save(follow);

		// 이벤트 발생
		applicationEventPublisher.publishEvent(
			FollowEvent.create(sender.getUsername(), receiverId, senderId));

		return FollowConverter.toCreateResponse(follow);
	}

	/**
	 * 팔로우 취소 메서드
	 * senderId(memberId1)와 receiverId(memberId2)를 받아 followEntity 삭제
	 * 각 멤버의 followingCount, followeeCount 감소
	 *
	 * @param id, senderId, receiverId
	 * @return DeleteFollowResponse (DTO)
	 */
	@Transactional
	public DeleteFollowResponse deleteFollow(Long id, Long senderId, Long receiverId) {
		// 1. 팔로우 관계가 적용되어 있는지 검증하고 엔티티 가져오기
		FollowEntity follow = followRepository.findById(id)
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "팔로우 확인에 실패했습니다."));

		// 2. sender, receiver 검증
		if (!follow.getSenderId().equals(senderId) || !follow.getReceiverId().equals(receiverId)) {
			throw new SocialException(SocialErrorCode.DATA_MISMATCH);
		}

		// 3. 팔로우 취소 및 팔로워, 팔로위 인원수 감소 반영
		memberRepository.decrementFollowerCount(senderId);
		memberRepository.decrementFolloweeCount(receiverId);
		followRepository.delete(follow);

		return FollowConverter.toDeleteResponse(follow);
	}

	@Transactional
	public boolean findMutualFollow(Long currentMemberId, Long memberId) {
		return followRepository.countMutualFollow(currentMemberId, memberId) == 2;
	}
}
