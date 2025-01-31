package com.example.backend.content.post.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.post.dto.PostCreateRequest;
import com.example.backend.content.post.dto.PostCreateResponse;
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
	 * @throws PostException 존재하지 않는 회원일 경우 예외 발생
	 */
	@Transactional
	public PostCreateResponse createPost(PostCreateRequest request) {
		MemberEntity memberEntity = memberRepository.findById(request.getMemberId())
			.orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));
		//MEMBER 클래스 EXCEPTION 으로 변경 예정
		PostEntity postEntity = request.toEntity(memberEntity);

		PostEntity savedPost = postRepository.save(postEntity);

		return PostCreateResponse.fromEntity(savedPost);
	}

}
