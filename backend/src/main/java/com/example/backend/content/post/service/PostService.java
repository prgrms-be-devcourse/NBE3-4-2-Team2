package com.example.backend.content.post.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.image.dto.ImageUploadResponse;
import com.example.backend.content.image.service.ImageService;
import com.example.backend.content.post.converter.PostConverter;
import com.example.backend.content.post.dto.PostCreateRequest;
import com.example.backend.content.post.dto.PostCreateResponse;
import com.example.backend.content.post.dto.PostDeleteResponse;
import com.example.backend.content.post.dto.PostModifyRequest;
import com.example.backend.content.post.dto.PostModifyResponse;
import com.example.backend.content.post.exception.PostErrorCode;
import com.example.backend.content.post.exception.PostException;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 게시물 관련 Service
 *
 * @author joonaeng
 * @since 2025-01-31
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;
	private final MemberRepository memberRepository;
	private final HashtagExtractor hashtagExtractor;
	private final PostHashtagService postHashtagService;
	private final ImageService imageService;

	/**
	 * createPost 요청을 받고 게시물을 생성하는 메소드
	 *
	 * @param request 게시물 생성 요청 객체
	 * @return PostCreateResponse 객체
	 * @throws PostException 존재하지 않는 회원일 경우 예외 발생
	 */
	@Transactional
	public PostCreateResponse createPost(PostCreateRequest request) {

		MemberEntity memberEntity = memberRepository.findById(request.memberId())
			.orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

		PostEntity postEntity = PostEntity.create(request.content(), memberEntity);
		PostEntity savedPost = postRepository.save(postEntity);

		// 이미지 업로드 및 URL 리스트 반환
		List<String> uploadedFileNames = new ArrayList<>();
		if (request.images() != null && !request.images().isEmpty()) {
			ImageUploadResponse imgUploadResp = imageService.uploadImages(postEntity.getId(), request.images());
			uploadedFileNames = imgUploadResp.images();
		}

		// 해시태그 추출 및 생성
		Set<String> extractHashtags = hashtagExtractor.extractHashtag(savedPost.getContent());
		postHashtagService.create(savedPost, extractHashtags);

		// PostCreateResponse에 이미지 URL 리스트 추가
		return PostCreateResponse.builder()
			.id(postEntity.getId())
			.content(postEntity.getContent())
			.memberId(memberEntity.getId())
			.imgUrlList(uploadedFileNames)
			.build();
	}

	@Transactional
	public PostModifyResponse modifyPost(Long postId, PostModifyRequest request) {
		PostEntity postEntity = postRepository.findByIdAndIsDeletedFalse(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

		if (!postEntity.getMember().getId().equals(request.memberId())) {
			throw new PostException(PostErrorCode.POST_UPDATE_FORBIDDEN);
		}

		postEntity.modifyContent(request.content());

		return PostConverter.toModifyResponse(postEntity);
	}

	@Transactional
	public PostDeleteResponse deletePost(Long postId, Long memberId) {
		PostEntity postEntity = postRepository.findByIdAndIsDeletedFalse(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

		if (!postEntity.getMember().getId().equals(memberId)) {
			throw new PostException(PostErrorCode.POST_DELETE_FORBIDDEN);
		}

		// imageService.deleteImages(postEntity);
		postEntity.deleteContent();

		return PostConverter.toDeleteResponse(postId);
	}

}
