package com.example.backend.social.reaction.likes.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.LikesEntity;
import com.example.backend.entity.LikesRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
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

	@Transactional
	public LikesEntity createLike(Long memberId, Long postId) {
		MemberEntity member = memberRepository.findById(memberId)
			.orElseThrow(() -> new LikesException(LikesErrorCode.MEMBER_NOT_FOUND));
		PostEntity post = postRepository.findById(postId)
			.orElseThrow(() -> new LikesException(LikesErrorCode.POST_NOT_FOUND));

		LikesEntity like = new LikesEntity(member, post, LocalDateTime.now());
		return likesRepository.save(like);
	}

	@Transactional
	public void deleteLike(Long memberId, Long postId) {
		LikesEntity like = likesRepository.findByMemberIdAndPostId(memberId, postId)
				.orElseThrow(() -> new LikesException(LikesErrorCode.LIKE_NOT_FOUND));
		likesRepository.deleteByMemberIdAndPostId(memberId, postId);
	}
}
