package com.example.backend.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {
	boolean existByMemberIdAndPostId(Long memberId, Long postId);
}
