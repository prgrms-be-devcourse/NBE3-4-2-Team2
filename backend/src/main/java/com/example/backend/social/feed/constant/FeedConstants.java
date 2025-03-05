package com.example.backend.social.feed.constant;

/**
 * FeedConstants
 * 피드 로직과 관련된 상수 클래스
 * @author ChoiHyunSan
 * @since 2025-02-03
 */
public class FeedConstants {

	/**
	 *  RECOMMEND_RANDOM_POOL_MULTIPLIER
	 *  추천 게시물에서 요청 개수만큼 뽑기 위해 배수 값으로 DB에 요청하기 위한 상수
	 */
	public static final long RECOMMEND_RANDOM_POOL_MULTIPLIER = 2L;

	/**
	 *  FOLLOWING_FEED_RATE
	 *  요청 게시물에서 팔로잉 게시물의 비율
	 */
	public static final double FOLLOWING_FEED_RATE = 0.5;

	/**
	 *  RECOMMEND_FEED_RATE
	 *  요청 게시물에서 추천 게시물의 비율
	 */
	public static final double RECOMMEND_FEED_RATE = 1.0 - FOLLOWING_FEED_RATE;

	/**
	 * RECOMMEND_SEARCH_DATE_RANGE
	 * 추천 게시물만 내보내는 경우 탐색 범위로 지정하는 날짜 범위 (일 단위)
	 */
	public static final long RECOMMEND_SEARCH_DATE_RANGE = 1;

	/**
	 * POPULAR_HASHTAG_COUNT
	 * 인기 해시태그를 선정하는 개수
	 */
	public static final long POPULAR_HASHTAG_COUNT = 10;

	/**
	 * REQUEST_FEED_MAX_SIZE
	 * 피드 요청 시 최대로 요청할 수 있는 개수
	 */
	public static final int REQUEST_FEED_MAX_SIZE = 20;
}
