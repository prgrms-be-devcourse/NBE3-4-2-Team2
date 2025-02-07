package com.example.backend.entity;

public interface FollowRepositoryCustom {
	boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
}
