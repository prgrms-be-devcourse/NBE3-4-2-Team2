package com.example.backend.social.follow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.entity.FollowEntity;
import com.example.backend.entity.FollowRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.social.follow.converter.FollowConverter;
import com.example.backend.social.follow.dto.CreateFollowResponse;
import com.example.backend.social.follow.dto.DeleteFollowResponse;
import com.example.backend.social.follow.exception.FollowErrorCode;
import com.example.backend.social.follow.exception.FollowException;

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

	@Autowired
	public FollowService(FollowRepository followRepository, MemberRepository memberRepository) {
		this.followRepository = followRepository;
		this.memberRepository = memberRepository;
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

		// 5. 팔로우 요청 및 팔로워, 팔로위 인원수 증가 반영
		memberRepository.incrementFollowerCount(senderId);
		memberRepository.incrementFolloweeCount(receiverId);
		followRepository.save(follow);

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
			.orElseThrow(() -> new FollowException(FollowErrorCode.FOLLOW_NOT_FOUND));

		// 2. 팔로우 취소를 요청한 멤버 ID와 senderId가 일치한지 검증
		if (!follow.getSender().equals(senderId)) {
			throw new FollowException(FollowErrorCode.SENDER_MISMATCH);
		}

		// 3. 팔로우 취소 요청의 상대 멤버 ID와 receiverId가 일치한지 검증
		if (!follow.getReceiver().equals(receiverId)) {
			throw new FollowException(FollowErrorCode.RECEIVER_MISMATCH);
		}

		// 4. 팔로우 취소 및 팔로워, 팔로위 인원수 감소 반영
		memberRepository.decrementFollowerCount(senderId);
		memberRepository.decrementFolloweeCount(receiverId);
		followRepository.delete(follow);

		return FollowConverter.toDeleteResponse(follow);
	}
}
