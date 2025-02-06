package com.example.backend.entity;

public interface BookmarkRepositoryCustom {
	boolean existsByMemberIdAndPostId(Long memberId, Long postId);
}
