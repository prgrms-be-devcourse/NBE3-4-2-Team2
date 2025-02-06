package com.example.backend.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
	Optional<PostEntity> findByIdAndIsDeletedFalse(Long postid);

	/**
	 * 좋아요 카운트 증가
	 * 좋아요 적용시 게시물의 좋아요 카운트를 1 증가시킴
	 *
	 */
	@Modifying
	@Query("UPDATE PostEntity post SET post.likeCount = post.likeCount + 1 WHERE post.id = :postId")
	int incrementLikeCount(@Param("postId") Long postId);

	/**
	 * 좋아요 카운트 감수
	 * 좋아요 적용시 게시물의 좋아요 카운트를 1 감소시킴
	 *
	 */
	@Modifying
	@Query("UPDATE PostEntity post SET post.likeCount = post.likeCount - 1 WHERE post.id = :postId AND post.likeCount > 0")
	int decrementLikeCount(@Param("postId") Long postId);
}
