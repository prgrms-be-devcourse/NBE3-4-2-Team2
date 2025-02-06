package com.example.backend.social.reaction.likes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
	@ResponseStatus(HttpStatus.OK)
	public RsData<CreateLikeResponse> likePost(@Valid @RequestBody CreateLikeRequest createRequest) {
		CreateLikeResponse createResponse = likesService.createLike(
			createRequest.memberId(), createRequest.postId()
		);
		return RsData.success(createResponse, "좋아요가 성공적으로 적용되었습니다.");
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.OK)
	public RsData<DeleteLikeResponse> unlikePost(@Valid @RequestBody DeleteLikeRequest deleteRequest) {
		DeleteLikeResponse deleteResponse = likesService.deleteLike(
			deleteRequest.id(), deleteRequest.memberId(), deleteRequest.postId()
		);
		return RsData.success(deleteResponse, "좋아요가 성공적으로 취소되었습니다.");
	}
}
