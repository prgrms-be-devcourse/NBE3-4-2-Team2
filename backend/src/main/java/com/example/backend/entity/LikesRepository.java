package com.example.backend.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikesRepository extends JpaRepository<LikesEntity, Long> {
	@Query("""
		SELECT CASE WHEN COUNT(like) > 0
		THEN true ELSE false END
		FROM LikesEntity like
		WHERE like.member.id = :memberId
		AND like.post.id = :postId
		""")
	boolean existsByMemberIdAndPostId(@Param("memberId") Long memberId, @Param("postId") Long postId);
}
