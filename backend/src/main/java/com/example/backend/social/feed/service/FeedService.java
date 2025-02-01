package com.example.backend.social.feed.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.social.feed.Feed;
import com.example.backend.social.feed.dto.FeedRequest;
import com.example.backend.social.feed.dto.FeedResponse;
import com.example.backend.social.feed.exception.FeedSampleException;
import com.example.backend.social.feed.implement.FeedSelector;
import com.example.backend.social.feed.implement.FeedValidator;

import lombok.RequiredArgsConstructor;

/***
 * FeedService
 * 피드에 관한 비즈니스 로직을 처리하는 컴포넌트
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@Service
@RequiredArgsConstructor
public class FeedService {

	private final FeedSelector feedFinder;
	private final MemberRepository memberRepository;
	private final FeedValidator feedValidator;

	/**
	 * Feed 요청 시에 적절한 게시물을 취합하여 반환하는 메서드
	 * @param request Feed 요청 시에 클라이언트에서 전달하는 Request 객체
	 * @param userId Feed 요청하는 유저의 ID
	 * @return Feed 객체를 클라이언트 요청 정보를 Response 형태로 매핑한 리스트
	 */
	@Transactional(readOnly = true)
	public List<FeedResponse> findList(FeedRequest request, Long userId) {

		feedValidator.validateRequest(request);

		// Member 검색.
		// 멤버 요청 건에 대한 예외 처리는 다른 도메인에서도 자주 사용될 것으로 예상되므로 우선 샘플 예외로 처리 후 수정
		MemberEntity member = memberRepository.findById(userId)
			.orElseThrow(() -> new FeedSampleException("Not Found"));

		// 1. 팔로잉 중인 유저들의 피드 검색
		// - 요청된 최대 개수의 70% 를 우선적으로 검색
		int followingCount = request.getMaxSize() * 100 / 70;
		List<Feed> feedList = feedFinder.findByFollower(member, request.getTimestamp(), followingCount);

		// 2. 추천 게시물 피드 검색
		// - 팔로잉 게시물의 개수에 따라 추천 게시물 요청 개수를 조정
		//   ( 요청 개수의 30% ) + ( 팔로우 게시물 요청 개수 - 실제 검색된 게시글 개수 )
		int recommendCount = (request.getMaxSize() * 100 / 30) + (followingCount - feedList.size());
		List<Feed> recommendFeedList = feedFinder.findRecommendFinder(request.getTimestamp(), recommendCount);

		// 3. 조회한 게시글 취합해서 Response 로 반환
		feedList.addAll(recommendFeedList);

		return feedList.stream()
			.map(FeedResponse::toResponse)
			.toList();
	}
}
