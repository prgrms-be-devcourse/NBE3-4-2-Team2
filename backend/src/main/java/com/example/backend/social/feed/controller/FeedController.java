package com.example.backend.social.feed.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.global.rs.RsData;
import com.example.backend.social.feed.dto.FeedInfoResponse;
import com.example.backend.social.feed.dto.FeedListResponse;
import com.example.backend.social.feed.dto.FeedMemberRequest;
import com.example.backend.social.feed.dto.FeedMemberResponse;
import com.example.backend.social.feed.dto.FeedRequest;
import com.example.backend.social.feed.service.FeedService;
import com.querydsl.jpa.impl.JPAQueryFactory;

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
public class FeedController {

	private final FeedService feedService;
	private final JPAQueryFactory queryFactory;

	/**
	 * 팔로잉 게시물과 추천 게시물이 혼합된 피드 리스트 요청
	 * @param request 요청 정보
	 * @return 피드 Dto 리스트
	 */
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public RsData<FeedListResponse> findFeedList(
		@RequestBody FeedRequest request
	) {
		return RsData.success(feedService.findList(request), "피드를 성공적으로 반환했습니다.");
	}

	/**
	 * 단건 게시글에 대한 피드정보 요청
	 * @param postId 게시글 ID
	 * @param username 요청하는 유저의 이름 (임시. 삭제 예정)
	 * @return 피드 Dto
	 */
	@GetMapping("/{postId}/{username}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<FeedInfoResponse> findFeedInfo(
		@PathVariable Long postId,
		@PathVariable String username
	) {
		return RsData.success(feedService.findByPostId(postId, username));
	}

	/**
	 * 특정 멤버가 작성한 게시글에 대한 피드정보 요청
	 * @param request 요청 정보
	 * @return 피드 Dto 리스트
	 */
	@GetMapping("/member")
	@ResponseStatus(HttpStatus.OK)
	public RsData<FeedMemberResponse> findMemberFeedList(
		@RequestBody FeedMemberRequest request
	) {
		return RsData.success(feedService.findMembersList(request));
	}
}
