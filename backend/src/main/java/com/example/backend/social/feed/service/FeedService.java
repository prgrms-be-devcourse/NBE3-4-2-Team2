package com.example.backend.social.feed.service;

import static com.example.backend.social.feed.constant.FeedConstants.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.global.exception.GlobalException;
import com.example.backend.identity.member.exception.MemberErrorCode;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.social.feed.Feed;
import com.example.backend.social.feed.converter.FeedConverter;
import com.example.backend.social.feed.dto.FeedInfoResponse;
import com.example.backend.social.feed.dto.FeedListResponse;
import com.example.backend.social.feed.dto.FeedMemberRequest;
import com.example.backend.social.feed.dto.FeedRequest;
import com.example.backend.social.feed.implement.FeedSelector;
import com.example.backend.social.feed.implement.FeedValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***
 * FeedService
 * 피드에 관한 비즈니스 로직을 처리하는 컴포넌트
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

	private final FeedSelector feedFinder;
	private final MemberService memberService;
	private final FeedValidator feedValidator;
	private final FeedConverter feedConverter;
	private final FeedSelector feedSelector;

	/**
	 * Feed 요청 시에 적절한 게시물을 취합하여 반환하는 메서드
	 * @param request Feed 요청 시에 클라이언트에서 전달하는 Request 객체
	 * @return Feed 객체를 클라이언트 요청 정보를 Response 형태로 매핑한 리스트
	 */
	@Transactional(readOnly = true)
	public FeedListResponse findList(FeedRequest request) {

		feedValidator.validateRequest(request);

		MemberEntity member = memberService.findByUsername(request.username())
			.orElseThrow(() -> new GlobalException(MemberErrorCode.NOT_FOUND));

		int followingCount = (int)(request.maxSize() * FOLLOWING_FEED_RATE);
		List<Feed> feedList = feedFinder.findByFollower(
			member, request.lastPostId(), followingCount);

		LocalDateTime lastTime = feedList.isEmpty()
			? request.timestamp().minusDays(RECOMMEND_SEARCH_DATE_RANGE)
			: feedList.getLast().getPost().getCreateDate();

		Long lastPostId = feedList.isEmpty()
			? request.lastPostId()
			: feedList.getLast().getPost().getId();

		int recommendCount = (int)(request.maxSize() * RECOMMEND_FEED_RATE) + (followingCount - feedList.size());
		List<Feed> recommendFeedList = feedFinder.findRecommendFinder(
			member,
			request.timestamp(),
			lastTime, recommendCount);

		feedList.addAll(recommendFeedList);

		List<FeedInfoResponse> feedDtoList = feedList.stream()
			.sorted(Comparator.comparing(
				(Feed feed) -> feed.getPost().getCreateDate()).reversed()
			)
			.map(feedConverter::toFeedInfoResponse)
			.toList();

		return FeedListResponse.create(
			feedDtoList,
			lastTime,
			lastPostId
		);
	}

	public FeedInfoResponse findByPostId(Long postId, String username) {

		MemberEntity member = memberService.findByUsername(username)
			.orElseThrow(() -> new GlobalException(MemberErrorCode.NOT_FOUND));

		Feed feed = feedSelector.findByPostId(postId, member);
		return feedConverter.toFeedInfoResponse(feed);
	}

	public List<FeedInfoResponse> findMembersList(FeedMemberRequest request) {

		feedValidator.validateRequest(request);

		MemberEntity member = memberService.findByUsername(request.username())
			.orElseThrow(() -> new GlobalException(MemberErrorCode.NOT_FOUND));

		return feedFinder.findMembers(member, request.lastPostId(), request.maxSize()).stream()
			.map(feedConverter::toFeedInfoResponse)
			.toList();
	}
}
