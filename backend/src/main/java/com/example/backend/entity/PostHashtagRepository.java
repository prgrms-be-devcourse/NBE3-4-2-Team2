package com.example.backend.entity;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostHashtagRepository extends JpaRepository<PostHashtagEntity, Long> {

	/**
	 * hashtagIds 를 기반으로 postHashtag 일괄 삭제
	 * 추후에 데이터 양 너무 커지면 배치처리 필요
	 * @author kwak
	 * @since 2025-02-03
	 */
	@Modifying(clearAutomatically = true)
	@Query("""
		DELETE FROM PostHashtagEntity ph
		WHERE ph.hashtag.id IN :hashtagIds
		""")
	void bulkDeleteByHashtagIds(@Param("hashtagIds") List<Long> hashtagIds);

	/**
	 * @author kwak
	 * @since 2025-02-04
	 */
	@Query("""
		SELECT ph FROM PostHashtagEntity ph
		JOIN FETCH ph.post p
		JOIN FETCH ph.hashtag h
		WHERE ph.post.id = :id
		""")
	List<PostHashtagEntity> findPostHashtagByPostId(@Param("id") Long postId);

	/**
	 * @author kwak
	 * @since 2025-02-04
	 */
	@Modifying(clearAutomatically = true)
	@Query("""
		DELETE FROM PostHashtagEntity ph
		WHERE ph.post.id = :postId
		AND ph.hashtag.content IN :deletedHashtagContents
		""")
	void deleteByPostIdAndHashtagContent(
		@Param("postId") Long postId, @Param("deletedHashtagContents") Set<String> deletedHashtagContents);

	/**
	 * 연결된 Post 사용할꺼면 추후에 개선 필요
	 * @author kwak
	 * @since 2025-02-04
	 */
	@Query("""
		SELECT ph FROM PostHashtagEntity ph
		JOIN FETCH ph.hashtag h
		WHERE ph.post.id = :id
		""")
	List<PostHashtagEntity> findByPostId(@Param("id") Long id);
}
