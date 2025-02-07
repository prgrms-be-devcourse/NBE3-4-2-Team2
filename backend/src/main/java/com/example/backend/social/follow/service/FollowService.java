package com.example.backend.social.follow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.entity.FollowEntity;
import com.example.backend.entity.FollowRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.social.follow.dto.CreateFollowResponse;
import com.example.backend.social.follow.exception.FollowErrorCode;
import com.example.backend.social.follow.exception.FollowException;

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

	@Autowired
	public FollowService(FollowRepository followRepository, MemberRepository memberRepository) {
		this.followRepository = followRepository;
		this.memberRepository = memberRepository;
	}

	/**
	 * 팔로우 요청 메서드
	 * senderId(memberId1)와 receiverId(memberId2)를 받아 followEntity 생성
	 * 각 멤버의 followingCount, followeeCount를 증가
	 *
	 * @param senderId, receiverId
	 * @return CreateFollowResponse (DTO)
	 */
	@Transactional
	public CreateFollowResponse createFollow(Long senderId, Long receiverId) {
		// 1. 팔로우 요청측 검증후 엔티티 가져오기
		MemberEntity sender = memberRepository.findById(senderId)
			.orElseThrow(() -> new FollowException(FollowErrorCode.USER_NOT_FOUND));

		// 2. 팔로위측 검증후 엔티티 가져오기
		MemberEntity receiver = memberRepository.findById(receiverId)
			.orElseThrow(() -> new FollowException(FollowErrorCode.USER_NOT_FOUND));

		// 3. 이미 팔로우가 되어있는지 검증
		if (followRepository.existsBySenderIdAndReceiverId(senderId, receiverId)) {
			throw new FollowException(FollowErrorCode.ALREADY_FOLLOWED);
		}

		// 4. id 및 요청 날짜 포함을 위해 엔티티 생성
		FollowEntity follow = FollowEntity.create(sender, receiver);

		// 5. 팔로우 요청 및 팔로워, 팔로위 카운트 증가 반영
		// TODO : MemberRepository 카운트 증감 메서드 생성
		// TODO : FollowConverter 변환 메서드 작성
	}
}
