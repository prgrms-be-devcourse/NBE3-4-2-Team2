package com.example.backend.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<LikesEntity, Long> {
	void deleteByMemberIdAndPostId(Long memberId, Long postId);

	Optional<LikesEntity> findByMemberIdAndPostId(Long memberId, Long postId);
}
