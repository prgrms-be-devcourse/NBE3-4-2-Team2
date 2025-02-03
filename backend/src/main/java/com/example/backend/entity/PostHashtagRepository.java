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
}
