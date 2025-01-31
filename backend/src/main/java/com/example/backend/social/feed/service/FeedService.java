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

	@Transactional(readOnly = true)
	public List<FeedResponse> findList(FeedRequest request, Long userId) {

		MemberEntity member = memberRepository.findById(userId)
			.orElseThrow(() -> new FeedSampleException("Not Found"));

		// 0. 팔로잉 게시물 / 추천 게시물 개수를
		int followingCount = request.getMaxSize() * 100 / 70;
		int recommendCount = request.getMaxSize() * 100 / 30;

		// 1. 팔로잉 중인 유저들의 게시글 & 추천 게시글 조회
		List<Feed> feedList = feedFinder.findByFollower(member, request.getTimestamp(), followingCount);
		List<Feed> recommendFeedList = feedFinder.findRecommendFinder(request.getTimestamp(), recommendCount);

		// 2. 조회한 게시글 취합해서 Response 로 반환
		feedList.addAll(recommendFeedList);

		return feedList.stream()
			.map(FeedResponse::toResponse)
			.toList();
	}
}
