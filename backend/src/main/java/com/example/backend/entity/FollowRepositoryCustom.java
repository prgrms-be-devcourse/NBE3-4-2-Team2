package com.example.backend.entity;

public interface FollowRepositoryCustom {
	boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

	int countMutualFollow(Long currentMemberId, Long memberId);
}
