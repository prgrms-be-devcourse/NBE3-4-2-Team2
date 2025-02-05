package com.example.backend.social.reaction.likes.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.global.rs.RsData;
import com.example.backend.social.reaction.likes.dto.CreateLikeRequest;
import com.example.backend.social.reaction.likes.dto.CreateLikeResponse;
import com.example.backend.social.reaction.likes.dto.DeleteLikeRequest;
import com.example.backend.social.reaction.likes.dto.DeleteLikeResponse;
import com.example.backend.social.reaction.likes.service.LikesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * LikesController
 * "/likes" 로 들어오는 요청 처리 컨트롤러
 *
 * @author Metronon
 * @since 2025-01-30
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api-v1/likes", produces = MediaType.APPLICATION_JSON_VALUE)
public class LikesController {
	private final LikesService likesService;

	@PostMapping
	public ResponseEntity<RsData<CreateLikeResponse>> likePost(@Valid @RequestBody CreateLikeRequest createLikeRequest) {
		CreateLikeResponse createLikeResponse = likesService.createLike(createLikeRequest.getMemberId(), createLikeRequest.getPostId());
		return ResponseEntity.ok()
			.body(
				RsData.success(createLikeResponse, "좋아요가 성공적으로 적용되었습니다.")
			);
	}

	@DeleteMapping
	public ResponseEntity<RsData<DeleteLikeResponse>> unlikePost(@Valid @RequestBody DeleteLikeRequest deleteLikeRequest) {
		DeleteLikeResponse deleteLikeResponse = likesService.deleteLike(deleteLikeRequest.getId(), deleteLikeRequest.getMemberId(), deleteLikeRequest.getPostId());
		return ResponseEntity.ok()
			.body(
				RsData.success(deleteLikeResponse, "좋아요가 성공적으로 취소되었습니다.")
			);
	}
}
