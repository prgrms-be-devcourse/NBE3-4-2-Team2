package com.example.backend.social.feed.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.global.rs.RsData;
import com.example.backend.identity.security.user.CustomUser;
import com.example.backend.social.feed.dto.FeedInfoResponse;
import com.example.backend.social.feed.dto.FeedListResponse;
import com.example.backend.social.feed.dto.FeedMemberRequest;
import com.example.backend.social.feed.dto.FeedMemberResponse;
import com.example.backend.social.feed.dto.FeedRequest;
import com.example.backend.social.feed.service.FeedService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * FeedController
 * "/feed" 로 들어오는 요청 처리 컨트롤러
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api-v1/feed")
@Tag(name = "FeedController", description = "API 피드 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class FeedController {

	private final FeedService feedService;

	/**
	 * 팔로잉 게시물과 추천 게시물이 혼합된 피드 리스트 요청
	 * @param request 요청 정보
	 * @return 피드 Dto 리스트
	 */
	@Operation(
		summary = "메인 피드 요청",
		description = "자신 및 팔로잉 게시물과 추천 게시물로 이뤄진 피드를 반환합니다.")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public RsData<FeedListResponse> findFeedList(
		@ModelAttribute FeedRequest request,
		@AuthenticationPrincipal CustomUser securityUser
	) {
		FeedListResponse response = feedService.findList(request, securityUser.getId());
		return RsData.success(response, "피드를 성공적으로 반환했습니다.");
	}

	/**
	 * 단건 게시글에 대한 피드정보 요청
	 * @param postId 게시글 ID
	 * @return 피드 Dto
	 */
	@Operation(
		summary = "단건 피드 요청",
		description = "단건 게시물을 피드로 반환합니다.")
	@GetMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<FeedInfoResponse> findFeedInfo(
		@PathVariable Long postId,
		@AuthenticationPrincipal CustomUser securityUser
	) {
		FeedInfoResponse response = feedService.findByPostId(postId, securityUser.getId());
		return RsData.success(response, "피드를 성공적으로 반환했습니다.");
	}

	/**
	 * 특정 멤버가 작성한 게시글에 대한 피드정보 요청
	 * @param request 요청 정보
	 * @return 피드 Dto 리스트
	 */
	@Operation(
		summary = "멤버 피드 요청",
		description = "해당 멤버의 게시물에 대한 피드를 요청합니다.")
	@GetMapping("/member")
	@ResponseStatus(HttpStatus.OK)
	public RsData<FeedMemberResponse> findMemberFeedList(
		@RequestBody FeedMemberRequest request,
		@AuthenticationPrincipal CustomUser securityUser
	) {
		FeedMemberResponse response = feedService.findMembersList(request, securityUser.getId());
		return RsData.success(response, "피드를 성공적으로 반환했습니다.");
	}
}
