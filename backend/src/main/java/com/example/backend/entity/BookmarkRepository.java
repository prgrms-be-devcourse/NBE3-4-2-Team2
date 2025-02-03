package com.example.backend.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {
	Optional<BookmarkEntity> findByMemberIdAndPostId(Long memberId, Long postId);
}
