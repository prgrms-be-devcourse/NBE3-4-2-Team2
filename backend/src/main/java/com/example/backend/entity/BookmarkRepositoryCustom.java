package com.example.backend.entity;

import java.util.List;

public interface BookmarkRepositoryCustom {
	boolean existsByMemberIdAndPostId(Long memberId, Long postId);
	List<BookmarkEntity> findAllByMemberId(Long memberId);
}
