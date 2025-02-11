package com.example.backend.content.post.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.content.post.dto.PostCreateRequest;
import com.example.backend.content.post.dto.PostCreateResponse;
import com.example.backend.content.post.dto.PostDeleteResponse;
import com.example.backend.content.post.dto.PostModifyRequest;
import com.example.backend.content.post.dto.PostModifyResponse;
import com.example.backend.content.post.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 게시물 관련 컨트롤러
 *
 * @author joonaeng
 * @since 2025-01-31
 */

@RestController
@RequestMapping("/api-v1/post")
@RequiredArgsConstructor
public class PostController {
	private final PostService postService;
	/**
	 * 게시물 생성 메서드
	 *
	 * @param request 게시물 생성 요청 객체
	 * @return 생성된 게시물 정보를 담은 응답 DTO
	 */
	@PostMapping
	public ResponseEntity<PostCreateResponse> createPost(@ModelAttribute @Valid PostCreateRequest request) {
		PostCreateResponse response = postService.createPost(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 게시물 수정 메서드
	 *
	 * @param postId 수정할 게시물의 ID
	 * @param request 게시물 수정 요청 객체
	 * @return 수정된 게시물 정보를 담은 응답 DTO
	 */
	@PutMapping("/{postId}")
	public ResponseEntity<PostModifyResponse> modifyPost(
		@PathVariable Long postId,
		@RequestBody @Valid PostModifyRequest request
	) {
		PostModifyResponse response = postService.modifyPost(postId, request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 게시물 삭제 메서드
	 *
	 * @param postId 삭제할 게시물의 ID
	 * @return 게시물 삭제 여부 정보를 담은 응답 DTO
	 */
	@DeleteMapping("/{postId}")
	public ResponseEntity<PostDeleteResponse> deletePost(
		@PathVariable Long postId,
		@RequestParam Long memberId
	) {
		PostDeleteResponse response = postService.deletePost(postId, memberId);
		return ResponseEntity.ok(response);
	}

}
