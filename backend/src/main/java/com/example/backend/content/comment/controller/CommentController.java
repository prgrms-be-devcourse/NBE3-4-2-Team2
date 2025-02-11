package com.example.backend.content.comment.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.content.comment.dto.CommentResponse;
import com.example.backend.content.comment.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api-v1/comment")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	/**
	 * ✅ 특정 게시글의 댓글 목록 조회 (페이징 적용)
	 */
	@GetMapping("/post/{postId}")
	public Page<CommentResponse> getComments(
		@PathVariable Long postId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
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
		@RequestParam(defaultValue = "10") int size
	) {
		Pageable pageable = Pageable.ofSize(size).withPage(page);
		return commentService.findRepliesByParentId(parentId, pageable);
	}
}
