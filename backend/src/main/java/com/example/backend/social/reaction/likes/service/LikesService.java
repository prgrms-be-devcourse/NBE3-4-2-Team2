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
import com.example.backend.social.reaction.likes.dto.LikesResponse;
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
	 * @param memberId
	 * @param postId
	 * @return LikesResponse (DTO)
	 */
	@Transactional
	public LikesResponse createLike(Long memberId, Long postId) {
		MemberEntity member = memberRepository.findById(memberId)
			.orElseThrow(() -> new LikesException(LikesErrorCode.MEMBER_NOT_FOUND));
		PostEntity post = postRepository.findById(postId)
			.orElseThrow(() -> new LikesException(LikesErrorCode.POST_NOT_FOUND));

		if (likesRepository.findByMemberIdAndPostId(memberId, postId).isPresent()) {
			throw new LikesException(LikesErrorCode.ALREADY_LIKE);
		}

		LikesEntity like = LikesEntity.builder()
			.member(member)
			.post(post)
			.build();

		likesRepository.save(like);

		return LikesResponse.toResponse(like);
	}

	/**
	 * 좋아요 취소 메서드
	 * memberId와 postId를 받아 LikesEntity 삭제
	 *
	 * @param memberId
	 * @param postId
	 */
	@Transactional
	public void deleteLike(Long memberId, Long postId) {
		LikesEntity like = likesRepository.findByMemberIdAndPostId(memberId, postId)
				.orElseThrow(() -> new LikesException(LikesErrorCode.LIKE_NOT_FOUND));

		likesRepository.delete(like);
	}
}
