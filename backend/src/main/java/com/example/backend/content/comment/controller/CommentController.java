package com.example.backend.content.comment.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.content.comment.dto.CommentCreateRequest;
import com.example.backend.content.comment.dto.CommentCreateResponse;
import com.example.backend.content.comment.dto.CommentDeleteResponse;
import com.example.backend.content.comment.dto.CommentModifyRequest;
import com.example.backend.content.comment.dto.CommentModifyResponse;
import com.example.backend.content.comment.dto.CommentResponse;
import com.example.backend.content.comment.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api-v1/comment")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	@PostMapping
	public ResponseEntity<CommentCreateResponse> createComment(@RequestBody CommentCreateRequest request) {
		return ResponseEntity.ok(commentService.createComment(request));
	}

	/**
	 * ✅ 특정 게시글의 댓글 목록 조회 (페이징 적용)
	 */
	@GetMapping("/post/{postId}")
	public Page<CommentResponse> getComments(
		@PathVariable Long postId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "50") int size
	) {
		Pageable pageable = Pageable.ofSize(size).withPage(page);
		return commentService.findAllCommentsByPostId(postId, pageable);
	}

	/**
	 * ✅ 특정 댓글의 대댓글 조회 (페이징 적용)
	 */
	@GetMapping("/replies/{parentId}")
	public Page<CommentResponse> getReplies(
		@PathVariable Long parentId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "50") int size
	) {
		Pageable pageable = Pageable.ofSize(size).withPage(page);
		return commentService.findRepliesByParentId(parentId, pageable);
	}

	/**
	 * ✅ 댓글 수정
	 */
	@PutMapping("/{commentId}")
	public ResponseEntity<CommentModifyResponse> modifyComment(
		@PathVariable Long commentId,
		@RequestBody CommentModifyRequest request
	) {
		return ResponseEntity.ok(commentService.modifyComment(commentId, request));
	}

	/**
	 * ✅ 댓글 삭제
	 */
	@DeleteMapping("/{commentId}")
	public ResponseEntity<CommentDeleteResponse> deleteComment(
		@PathVariable Long commentId,
		@RequestParam Long memberId
	) {
		return ResponseEntity.ok(commentService.deleteComment(commentId, memberId));
	}
}
