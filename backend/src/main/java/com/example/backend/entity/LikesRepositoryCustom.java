package com.example.backend.entity;

public interface LikesRepositoryCustom {
	boolean existsByMemberIdAndPostId(Long memberId, Long postId);
}
