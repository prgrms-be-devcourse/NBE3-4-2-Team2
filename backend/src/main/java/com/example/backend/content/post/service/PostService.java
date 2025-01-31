package com.example.backend.content.post.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.post.dto.PostRequest;
import com.example.backend.content.post.dto.PostResponse;
import com.example.backend.content.post.exception.PostErrorCode;
import com.example.backend.content.post.exception.PostException;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;

import lombok.RequiredArgsConstructor;

/**
 * 게시물 관련 Service
 *
 * @author joonaeng
 * @since 2025-01-31
 */

@Service
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;
	private final MemberRepository memberRepository;

	/**
	 * createPost 요청을 받고 게시물을 생성하는 메소드
	 *
	 * @param request 게시물 생성 요청 객체
	 * @return PostCreateResponse 객체
	 * @throws IllegalArgumentException 존재하지 않는 회원일 경우 예외 발생
	 */
	@Transactional
	public PostResponse createPost(PostRequest request) {
		MemberEntity memberEntity = memberRepository.findById(request.getMemberId())
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

		PostEntity postEntity = request.toEntity(memberEntity);

		PostEntity savedPost = postRepository.save(postEntity);

		return PostResponse.fromEntity(savedPost);
	}

}
