package com.example.backend.social.follow.service

import com.example.backend.entity.MemberRepository
import com.example.backend.global.event.FollowEvent
import com.example.backend.social.exception.SocialErrorCode
import com.example.backend.social.exception.SocialException
import com.example.backend.social.follow.converter.FollowConverter
import com.example.backend.social.follow.dto.FollowResponse
import com.example.backend.social.follow.dto.FollowerListResponse
import com.example.backend.social.follow.dto.FollowingListResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 팔로우 서비스
 * 팔로우 서비스 관련 로직 구현
 *
 * @author Metronon
 * @since 2025-03-06
 */
@Service
open class FollowService @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    // 본인을 팔로우 신청하는지 확인
    private fun checkNotSelfFollow(senderUsername: String, receiverUsername: String) {
        if (senderUsername == receiverUsername) {
            throw SocialException(SocialErrorCode.CANNOT_PERFORM_ON_SELF)
        }
    }
    /**
     * 팔로우 요청 메서드
     * sender(followingList add, followingCount ++)
     * receiver(followerList add, followerCount ++)
     *
     * @param senderUsername, receiverUsername
     * @return FollowResponse (DTO)
     */
    @Transactional
    open fun createFollow(senderUsername: String, receiverUsername: String): FollowResponse {
        // 1. 본인에게 요청하는지 확인
        checkNotSelfFollow(senderUsername, receiverUsername)

        // 2. 팔로우 요청측 검증후 엔티티 가져오기
        val sender = memberRepository.findByUsername(senderUsername)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "요청측 회원 검증에 실패했습니다.") }

        // 3. 팔로잉측(팔로우 받는 회원) 검증 후 엔티티 가져오기
        val receiver = memberRepository.findByUsername(receiverUsername)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "응답측 회원 검증에 실패했습니다.") }

        // 4. 이미 팔로우가 되어있는지 검증
        val alreadyFollowed = sender.followingList.stream()
            .anyMatch { member: String -> member == receiverUsername }

        if (alreadyFollowed) {
            throw SocialException(SocialErrorCode.ALREADY_EXISTS, "이미 팔로우 상태입니다.")
        }

        // 5. 팔로우 관계 생성 및 팔로우 카운트 증가
        sender.addFollowing(receiver)
        receiver.addFollower(sender)

        // 6. 팔로우 이벤트 발생
        applicationEventPublisher.publishEvent(
            FollowEvent.create(senderUsername, receiver.id, sender.id)
        )

        return FollowConverter.toResponse(sender, receiver)
    }

    /**
     * 팔로우 취소 메서드
     * sender(followingList remove, followingCount --)
     * receiver(followerList remove, followerCount --)
     *
     * @param senderUsername, receiverUsername
     * @return FollowResponse (DTO)
     */
    @Transactional
    open fun deleteFollow(senderUsername: String, receiverUsername: String): FollowResponse {
        // 1. 본인에게 요청하는지 확인
        checkNotSelfFollow(senderUsername, receiverUsername)

        // 2. 팔로우 요청측(취소 요청하는 회원) 검증 후 엔티티 가져오기
        val sender = memberRepository.findByUsername(senderUsername)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "요청측 회원 검증에 실패했습니다.") }

        // 3. 팔로잉측(팔로우 받는 회원) 검증 후 엔티티 가져오기
        val receiver = memberRepository.findByUsername(receiverUsername)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "응답측 회원 검증에 실패했습니다.") }

        // 4. 팔로우 관계 존재 여부 검증
        val isFollowing = sender.followingList.stream()
            .anyMatch { member: String -> member == receiver.username }
        if (!isFollowing) {
            throw SocialException(SocialErrorCode.NOT_FOUND, "팔로우 관계를 찾을 수 없습니다.")
        }

        // 5. 팔로우 취소 관계 처리 및 팔로우 카운트 감소
        sender.removeFollowing(receiver)
        receiver.removeFollower(sender)

        return FollowConverter.toResponse(sender, receiver)
    }

    /**
     * 맞팔로우 확인 메서드
     *
     * @param senderUsername, receiverUsername
     * @return boolean
     */
    @Transactional
    open fun isMutualFollow(senderUsername: String, receiverUsername: String): Boolean {
        // 1. 본인에게 요청하는지 확인
        checkNotSelfFollow(senderUsername, receiverUsername)

        // 2. 팔로우 요청 측(요청자) 검증 후 엔티티 가져오기
        val sender = memberRepository.findByUsername(senderUsername)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "요청측 회원 검증에 실패했습니다.") }

        // 3. 팔로잉 측(대상) 검증 후 엔티티 가져오기
        val receiver = memberRepository.findByUsername(receiverUsername)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "응답측 회원 검증에 실패했습니다.") }

        // 4. sender의 followingList에 receiver의 username이 있는지 확인
        val senderFollowsReceiver = sender.followingList.stream()
            .anyMatch { username: String -> username == receiver.username }

        // 5. receiver의 followingList에 sender의 username이 있는지 확인
        val receiverFollowsSender = receiver.followingList.stream()
            .anyMatch { username: String -> username == sender.username }

        // 둘 다 만족하면 맞팔로우로 간주
        return senderFollowsReceiver && receiverFollowsSender
    }

    /**
     * 팔로우 여부 확인 메서드
     *
     * @param senderUsername, receiverUsername
     * @return boolean
     */
    @Transactional(readOnly = true)
    open fun isFollowing(senderUsername: String, receiverUsername: String): Boolean {
        // 1. 팔로우 요청 측(요청자) 검증 후 엔티티 가져오기
        val sender = memberRepository.findByUsername(senderUsername)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "요청측 회원 검증에 실패했습니다.") }

        // 2. sender 의 followingList 에 receiverUsername 이 있는지 확인
        return sender.followingList.any { it == receiverUsername }
    }

    /**
     * 팔로잉 목록 조회 메서드
     *
     * @param username
     * @return FollowingListResponse (DTO)
     */
    @Transactional(readOnly = true)
    open fun getFollowingList(username: String): FollowingListResponse {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "회원 검증에 실패했습니다.") }

        val followingMembers = member.followingList.mapNotNull { followingUsername ->
            memberRepository.findByUsername(followingUsername).orElse(null)
        }

        return FollowConverter.toFollowingListResponse(followingMembers)
    }

    /**
     * 팔로워 목록 조회 메서드
     *
     * @param username
     * @return FollowerListResponse (DTO)
     */
    @Transactional(readOnly = true)
    open fun getFollowerList(username: String): FollowerListResponse {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "회원 검증에 실패했습니다.") }

        val followerMembers = member.followerList.mapNotNull { followerUsername ->
            memberRepository.findByUsername(followerUsername).orElse(null)
        }

        return FollowConverter.toFollowerListResponse(followerMembers)
    }
}
