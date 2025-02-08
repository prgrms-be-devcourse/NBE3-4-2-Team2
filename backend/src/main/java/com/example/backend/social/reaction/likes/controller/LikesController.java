package com.example.backend.social.reaction.likes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.global.rs.RsData;
import com.example.backend.identity.security.user.SecurityUser;
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

	@PostMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<CreateLikeResponse> likePost(
		@PathVariable Long postId,
		SecurityUser securityUser
	) {
		CreateLikeResponse createResponse = likesService.createLike(
			securityUser.getId(), postId
		);
		return RsData.success(createResponse, "좋아요가 성공적으로 적용되었습니다.");
	}

	@DeleteMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<DeleteLikeResponse> unlikePost(
		@PathVariable Long postId,
		@Valid @RequestBody DeleteLikeRequest deleteRequest,
		SecurityUser securityUser
		) {
		DeleteLikeResponse deleteResponse = likesService.deleteLike(
			deleteRequest.id(), securityUser.getId(), postId
		);
		return RsData.success(deleteResponse, "좋아요가 성공적으로 취소되었습니다.");
	}
}
