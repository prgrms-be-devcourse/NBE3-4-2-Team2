package com.example.backend.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
	Optional<MemberEntity> findByUsername(String username);

	Optional<MemberEntity> findByRefreshToken(String refreshToken);

	Optional<MemberEntity> findByEmail(String email);

	Optional<MemberEntity> findByPhoneNumber(String phoneNumber);

	/**
	 * 팔로워 카운트 증가 쿼리
	 * 팔로우 요청 시 요청멤버의 팔로워 카운트를 1 증가시킴
	 */
	@Modifying(clearAutomatically = true)
	@Query("UPDATE MemberEntity sender SET sender.followerCount = sender.followerCount + 1 WHERE sender.id = :senderId")
	void incrementFollowerCount(@Param("senderId") Long senderId);

	/**
	 * 팔로위 카운트 증가 쿼리
	 * 팔로우 요청 시 요청상대의 팔로위 카운트를 1 증가시킴
	 */
	@Modifying(clearAutomatically = true)
	@Query("UPDATE MemberEntity receiver SET receiver.followeeCount = receiver.followeeCount + 1 WHERE receiver.id = :receiverId")
	void incrementFolloweeCount(@Param("receiverId") Long receiverId);

	/**
	 * 팔로워 카운트 감소 쿼리
	 * 팔로우 취소 요청 시 요청멤버의 팔로워 카운트를 1 감소시킴
	 */
	@Modifying(clearAutomatically = true)
	@Query("UPDATE MemberEntity sender SET sender.followerCount = sender.followerCount - 1 WHERE sender.id = :senderId AND sender.followerCount > 0")
	void decrementFollowerCount(@Param("senderId") Long senderId);

	/**
	 * 팔로위 카운트 감소 쿼리
	 * 팔로우 취소 요청 시 요청상대의 팔로위 카운트를 1 감소시킴
	 */
	@Modifying(clearAutomatically = true)
	@Query("UPDATE MemberEntity receiver SET receiver.followeeCount = receiver.followeeCount - 1 WHERE receiver.id = :receiverId AND receiver.followeeCount > 0")
	void decrementFolloweeCount(@Param("receiverId") Long receiverId);
}
