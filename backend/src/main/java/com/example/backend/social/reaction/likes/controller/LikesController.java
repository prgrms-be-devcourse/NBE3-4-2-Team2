package com.example.backend.social.reaction.likes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "LikeController", description = "좋아요 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class LikesController {
	private final LikesService likesService;

	/**
	 * 게시물의 ID를 통해 좋아요를 반영합니다.
	 * @param postId, securityUser
	 * @return createLikeResponse (DTO)
	 */
	@Operation(summary = "게시물 좋아요 요청", description = "게시물에 좋아요 요청을 보냅니다.")
	@PostMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<CreateLikeResponse> likePost(
		@PathVariable Long postId,
		@AuthenticationPrincipal SecurityUser securityUser
	) {
		CreateLikeResponse createResponse = likesService.createLike(
			securityUser.getId(), postId
		);
		return RsData.success(createResponse, "좋아요가 성공적으로 적용되었습니다.");
	}

	/**
	 * 게시물의 ID를 통해 좋아요를 취소합니다.
	 * @param postId, deleteLikeRequest(LikeId), securityUser
	 * @return deleteLikeResponse (DTO)
	 */
	@Operation(summary = "게시물 좋아요 취소", description = "게시물에 좋아요 취소 요청을 보냅니다.")
	@DeleteMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<DeleteLikeResponse> unlikePost(
		@Valid @RequestBody DeleteLikeRequest deleteRequest,
		@AuthenticationPrincipal SecurityUser securityUser,
		@PathVariable Long postId
		) {
		DeleteLikeResponse deleteResponse = likesService.deleteLike(
			deleteRequest.likeId(), securityUser.getId(), postId
		);
		return RsData.success(deleteResponse, "좋아요가 성공적으로 취소되었습니다.");
	}
}
