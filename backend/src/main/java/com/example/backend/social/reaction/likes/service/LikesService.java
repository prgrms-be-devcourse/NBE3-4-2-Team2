package com.example.backend.social.reaction.likes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.LikesEntity;
import com.example.backend.entity.LikesRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.social.reaction.likes.converter.LikesConverter;
import com.example.backend.social.reaction.likes.dto.CreateLikeResponse;
import com.example.backend.social.reaction.likes.dto.DeleteLikeResponse;
import com.example.backend.social.reaction.likes.exception.LikesErrorCode;
import com.example.backend.social.reaction.likes.exception.LikesException;

/**
 * 좋아요 서비스
 * 좋아요 서비스 관련 로직 구현
 *
 * @author Metronon
 * @since 2025-01-30
 */
@Service
public class LikesService {
	private final LikesRepository likesRepository;
	private final MemberRepository memberRepository;
	private final PostRepository postRepository;

	@Autowired
	public LikesService(LikesRepository likesRepository, MemberRepository memberRepository, PostRepository postRepository) {
		this.likesRepository = likesRepository;
		this.memberRepository = memberRepository;
		this.postRepository = postRepository;
	}
	/**
	 * 좋아요 생성 메서드
	 * memberId와 postId를 받아 LikesEntity 생성
	 *
	 * @param memberId, postId
	 * @return CreateLikeResponse (DTO)
	 */
	@Transactional
	public CreateLikeResponse createLike(Long memberId, Long postId) {
		// 1. 멤버가 존재하는지 검증하고 엔티티 가져오기
		MemberEntity member = memberRepository.findById(memberId)
			.orElseThrow(() -> new LikesException(LikesErrorCode.MEMBER_NOT_FOUND));

		// 2. 게시물이 존재하는지 검증하고 엔티티 가져오기
		PostEntity post = postRepository.findById(postId)
			.orElseThrow(() -> new LikesException(LikesErrorCode.POST_NOT_FOUND));

		// 3. 이미 적용된 좋아요인지 검증
		if (likesRepository.existsByMemberIdAndPostId(memberId, postId)) {
			throw new LikesException(LikesErrorCode.ALREADY_LIKED);
		}

		// 4. id 및 생성 날짜를 포함하기 위해 build
		LikesEntity like = LikesEntity.create(member, post);

		// 5. 좋아요 생성 및 좋아요 횟수 증가 반영
		postRepository.incrementLikeCount(postId);
		likesRepository.save(like);

		return LikesConverter.toCreateResponse(like);
	}

	/**
	 * 좋아요 취소 메서드
	 * memberId와 postId를 받아 LikesEntity 삭제
	 *
	 * @param id, memberId, postId
	 * @return DeleteLikeResponse (DTO)
	 */
	@Transactional
	public DeleteLikeResponse deleteLike(Long id, Long memberId, Long postId) {
		// 1. 좋아요가 실제로 적용되어 있는지 검증
		LikesEntity like = likesRepository.findById(id)
			.orElseThrow(() -> new LikesException(LikesErrorCode.LIKE_NOT_FOUND));

		// 2. 좋아요의 멤버 ID와 요청한 멤버 ID가 동일한지 검증
		if (!like.getMemberId().equals(memberId)) {
			throw new LikesException(LikesErrorCode.MEMBER_MISMATCH);
		}

		// 3. 좋아요의 게시물 ID와 요청한 게시물 ID가 동일한지 검증
		if (!like.getPostId().equals(postId)) {
			throw new LikesException(LikesErrorCode.POST_MISMATCH);
		}

		// 4. 좋아요 취소 및 좋아요 횟수 감소 반영
		postRepository.decrementLikeCount(postId);
		likesRepository.delete(like);

		return LikesConverter.toDeleteResponse(like);
	}
}
