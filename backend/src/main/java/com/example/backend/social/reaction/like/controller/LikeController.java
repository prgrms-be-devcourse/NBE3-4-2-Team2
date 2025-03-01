package com.example.backend.social.reaction.like.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.global.rs.RsData;
import com.example.backend.identity.security.user.CustomUser;
import com.example.backend.social.reaction.like.dto.LikeToggleResponse;
import com.example.backend.social.reaction.like.service.LikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * LikeController
 * "/like" 로 들어오는 요청 처리 컨트롤러
 *
 * @author Metronon
 * @since 2025-01-30
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api-v1/like", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "LikeController", description = "좋아요 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class LikeController {
	private final LikeService likeService;

	/**
	 * 좋아요를 토글합니다 (없으면 생성, 있으면 상태 변경)
	 */
	@Operation(summary = "좋아요 토글", description = "게시물, 댓글, 대댓글의 좋아요를 토글합니다.")
	@PostMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<LikeToggleResponse> toggleLike(
		@PathVariable Long id,
		@RequestParam String resourceType, // post, comment, reply
		@AuthenticationPrincipal CustomUser securityUser
	) {
		LikeToggleResponse response = likeService.toggleLike(securityUser.getId(), resourceType, id);
		String message = response.isLiked()
			? "좋아요가 성공적으로 적용되었습니다."
			: "좋아요가 성공적으로 취소되었습니다.";
		return RsData.success(response, message);
	}
}
