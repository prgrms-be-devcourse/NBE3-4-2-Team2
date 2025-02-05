package com.example.backend.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<LikesEntity, Long> {
	Optional<LikesEntity> findByMemberIdAndPostId(Long memberId, Long postId);
}
