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
import com.example.backend.social.follow.dto.FollowerListResponse;
import com.example.backend.social.follow.dto.FollowingListResponse;
import com.example.backend.social.follow.dto.IsFollowingResponse;
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
	@Operation(summary = "맞팔로우 확인", description = "상대 멤버와 팔로우 관계인지 확인합니다.")
	@GetMapping("/mutual/{receiver}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<MutualFollowResponse> isMutualFollow(
		@PathVariable String receiver,
		@AuthenticationPrincipal CustomUser securityUser
	) {
		boolean isMutualFollow = followService.isMutualFollow(
			securityUser.getUsername(), receiver
		);

		MutualFollowResponse getResponse = new MutualFollowResponse(isMutualFollow);

		return RsData.success(getResponse, "맞팔로우 여부 조회에 성공했습니다.");
	}

	/**
	 * 상대방을 팔로우 중인지 확인
	 *
	 * @param securityUser(본인), receiver(상대)
	 * @return IsFollowingResponse (DTO)
	 */
	@Operation(summary = "팔로우 여부 확인", description = "상대 멤버를 팔로우하고 있는지 확인합니다.")
	@GetMapping("/following/{receiver}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<IsFollowingResponse> isFollowing(
		@PathVariable String receiver,
		@AuthenticationPrincipal CustomUser securityUser
	) {
		boolean isFollowing = followService.isFollowing(
			securityUser.getUsername(), receiver
		);

		IsFollowingResponse getResponse = new IsFollowingResponse(isFollowing);

		return RsData.success(getResponse, "팔로우 여부 조회에 성공했습니다.");
	}

	/**
	 * 내가 팔로우하고 있는 유저 List (Following)
	 *
	 * @param securityUser(본인)
	 * @return FollowingListResponse (DTO)
	 */
	// @Operation(summary = "팔로잉 목록 조회", description = "내가 팔로우하고 있는 멤버 목록을 조회합니다.")
	// @GetMapping("/following")
	// @ResponseStatus(HttpStatus.OK)
	// public RsData<FollowingListResponse> getFollowingList(
	// 	@AuthenticationPrincipal CustomUser securityUser
	// ) {
	// 	FollowingListResponse listResponse = followService.getFollowingList(
	// 		securityUser.getUsername()
	// 	);
	//
	// 	return RsData.success(listResponse, "팔로잉 목록 조회에 성공했습니다.");
	// }
	@Operation(summary = "팔로잉 목록 조회", description = "특정 사용자가 팔로우하고 있는 멤버 목록을 조회합니다.")
	@GetMapping("/{username}/following")
	@ResponseStatus(HttpStatus.OK)
	public RsData<FollowingListResponse> getFollowingList(
		@PathVariable String username
	) {
		FollowingListResponse listResponse = followService.getFollowingList(username);

		return RsData.success(listResponse, "팔로잉 목록 조회에 성공했습니다.");
	}


	// /**
	//  * 나를 팔로우하고 있는 유저 List (Follower)
	//  *
	//  * @param securityUser(본인)
	//  * @return FollowerListResponse (DTO)
	//  */
	// @Operation(summary = "팔로워 목록 조회", description = "나를 팔로우하고 있는 멤버 목록을 조회합니다.")
	// @GetMapping("/followers")
	// @ResponseStatus(HttpStatus.OK)
	// public RsData<FollowerListResponse> getFollowerList(
	// 	@AuthenticationPrincipal CustomUser securityUser
	// ) {
	// 	FollowerListResponse listResponse = followService.getFollowerList(
	// 		securityUser.getUsername()
	// 	);
	//
	// 	return RsData.success(listResponse, "팔로워 목록 조회에 성공했습니다.");
	// }
	/**
	 * 특정 사용자를 팔로우하고 있는 유저 목록 (Follower)
	 *
	 * @param username 조회할 사용자의 username
	 * @return FollowerListResponse (DTO)
	 */
	@Operation(summary = "팔로워 목록 조회", description = "특정 사용자를 팔로우하고 있는 멤버 목록을 조회합니다.")
	@GetMapping("/{username}/followers")
	@ResponseStatus(HttpStatus.OK)
	public RsData<FollowerListResponse> getFollowerList(
		@PathVariable String username
	) {
		FollowerListResponse listResponse = followService.getFollowerList(username);

		return RsData.success(listResponse, "팔로워 목록 조회에 성공했습니다.");
	}

}
