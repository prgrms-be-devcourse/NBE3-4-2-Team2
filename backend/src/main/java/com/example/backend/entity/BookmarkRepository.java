package com.example.backend.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {
	@Query("""
		SELECT CASE WHEN COUNT(bookmark) > 0
		THEN true ELSE false END
		FROM BookmarkEntity bookmark
		WHERE bookmark.member.id = :memberId
		AND bookmark.post.id = :postId
		""")
	boolean existsByMemberIdAndPostId(@Param("memberId") Long memberId, @Param("postId") Long postId);
}
