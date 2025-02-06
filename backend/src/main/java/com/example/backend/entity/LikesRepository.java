package com.example.backend.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<LikesEntity, Long> {
	boolean existByMemberIdAndPostId(Long memberId, Long postId);
}
