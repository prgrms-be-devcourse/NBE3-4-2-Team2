package com.example.backend.social.follow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.global.rs.RsData;
import com.example.backend.identity.security.user.CustomUser;
import com.example.backend.social.follow.dto.FollowResponse;
import com.example.backend.social.follow.dto.MutualFollowResponse;
import com.example.backend.social.follow.service.FollowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * FollowController
 * "/member/follow" 로 들어오는 요청 처리 컨트롤러
 *
 * @author Metronon
 * @since 2025-03-06
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api-v1/member", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "FollowController", description = "팔로우 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class FollowController {
	private final FollowService followService;

	/**
	 * 다른 멤버를 대상으로 팔로우 요청
	 * @param securityUser(본인), receiver(상대)
	 * @return FollowResponse (DTO)
	 */
	@Operation(summary = "상대방 팔로우 요청", description = "상대 멤버와 팔로우 관계를 맺습니다.")
	@PostMapping("/follow/{receiver}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<FollowResponse> followMember(
		@PathVariable String receiver,
		@AuthenticationPrincipal CustomUser securityUser
	) {
		FollowResponse createResponse = followService.createFollow(
			securityUser.getUsername(), receiver
		);
		return RsData.success(createResponse, "팔로우 등록에 성공했습니다.");
	}

	/**
	 * 다른 멤버를 대상으로 팔로우 관계 취소 요청
	 * @param securityUser(본인), receiver(상대)
	 * @return FollowResponse (DTO)
	 */
	@Operation(summary = "상대방 팔로우 취소", description = "상대 멤버와 팔로우 관계를 끊습니다.")
	@DeleteMapping("/follow/{receiver}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<FollowResponse> unfollowMember(
		@PathVariable String receiver,
		@AuthenticationPrincipal CustomUser securityUser
	) {
		FollowResponse deleteResponse = followService.deleteFollow(
			securityUser.getUsername(), receiver
		);
		return RsData.success(deleteResponse, "팔로우 취소에 성공했습니다.");
	}

	/**
	 * 상대방과 맞팔로우 상태인지 확인
	 *
	 * @param securityUser(본인), receiver(상대)
	 * @return MutualFollowResponse (DTO)
	 */
	@Operation(summary = "맞팔로우 확인", description = "상대 멤버와 서로 팔로우 관계인지 확인합니다.")
	@GetMapping("/mutual/{receiver}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<MutualFollowResponse> isMutualFollow(
		@PathVariable String receiver,
		@AuthenticationPrincipal CustomUser securityUser
	) {
		boolean isMutualFollow = followService.findMutualFollow(securityUser.getId(), receiver);

		MutualFollowResponse getResponse = new MutualFollowResponse(isMutualFollow);

		return RsData.success(getResponse, "맞팔로우 여부 조회에 성공했습니다.");
	}
}
