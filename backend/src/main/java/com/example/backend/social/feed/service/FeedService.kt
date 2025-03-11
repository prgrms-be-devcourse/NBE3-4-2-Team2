package com.example.backend.social.feed.service

import com.example.backend.global.exception.GlobalException
import com.example.backend.identity.member.exception.MemberErrorCode
import com.example.backend.identity.member.service.MemberService
import com.example.backend.social.feed.constant.FeedConstants
import com.example.backend.social.feed.converter.FeedConverter
import com.example.backend.social.feed.dto.*
import com.example.backend.social.feed.implement.FeedSelectorCache
import com.example.backend.social.feed.implement.FeedValidator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * FeedService
 * 피드에 관한 비즈니스 로직을 처리하는 컴포넌트
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@Service
open class FeedService(
    private val memberService: MemberService,
    private val feedValidator: FeedValidator,
    private val feedConverter: FeedConverter,
    private val feedSelector: FeedSelectorCache
) {
    /**
     * Feed 요청 시에 적절한 게시물을 취합하여 반환하는 메서드
     * @param request Feed 요청 시에 클라이언트에서 전달하는 Request 객체
     * @param userId 요청한 사용자의 ID
     * @return Feed 객체를 클라이언트 요청 정보를 Response 형태로 매핑한 리스트
     */
    @Transactional(readOnly = true)
    open fun findList(request: FeedRequest, userId: Long): FeedListResponse {
        feedValidator.validateRequest(request)

        val member = memberService.findById(userId)
            .orElseThrow { GlobalException(MemberErrorCode.NOT_FOUND) }

        val followingCount = (request.maxSize * FeedConstants.FOLLOWING_FEED_RATE).toInt()
        val feedList = feedSelector.findByFollower(
            member, request.lastPostId, followingCount
        ).toMutableList()

        // 마지막 포스트 ID와 시간 추출 (안전하게 접근)
        val lastFeed = feedList.lastOrNull()
        val lastPostId = lastFeed?.post?.id ?: request.lastPostId

        val lastTime =
            lastFeed?.post?.createDate ?: request.timestamp.minusDays(FeedConstants.RECOMMEND_SEARCH_DATE_RANGE)

        val recommendCount = (request.maxSize * FeedConstants.RECOMMEND_FEED_RATE).toInt() +
                (followingCount - feedList.size)

        val recommendFeedList = feedSelector.findRecommendFinder(
            member,
            request.timestamp,
            lastTime,
            recommendCount
        )

        feedList.addAll(recommendFeedList)

        // 생성일 기준으로 정렬 (자바 호환성을 위해 메서드 호출 방식 사용)
        val feedDtoList = feedList
            .sortedByDescending { it.post.createDate }
            .map { feedConverter.toFeedInfoResponse(it) }

        return FeedListResponse.create(
            feedDtoList,
            lastTime,
            lastPostId
        )
    }

    /**
     * 특정 포스트 ID로 피드를 조회하는 메서드
     * @param postId 조회할 포스트 ID
     * @param userId 요청한 사용자의 ID
     * @return 조회된 피드 정보
     */
    @Transactional(readOnly = true)
    open fun findByPostId(postId: Long?, userId: Long): FeedInfoResponse {
        val member = memberService.findById(userId)
            .orElseThrow { GlobalException(MemberErrorCode.NOT_FOUND) }

        val feed = feedSelector.findByPostId(postId, member)
        return feedConverter.toFeedInfoResponse(feed)
    }

    /**
     * 특정 멤버의 피드 목록을 조회하는 메서드
     * @param request 피드 멤버 요청 객체
     * @param userId 요청한 사용자의 ID
     * @return 멤버의 피드 목록 응답
     */
    @Transactional(readOnly = true)
    open fun findMembersList(request: FeedMemberRequest, userId: Long): FeedMemberResponse {
        feedValidator.validateRequest(request)

        val member = memberService.findById(userId)
            .orElseThrow { GlobalException(MemberErrorCode.NOT_FOUND) }

        val feedList = feedSelector.findByMember(member, request.lastPostId, request.maxSize)
            .map { feedConverter.toFeedInfoResponse(it) }

        val lastPostId = feedList.lastOrNull()?.postId ?: request.lastPostId

        return FeedMemberResponse.create(feedList, lastPostId)
    }
}
