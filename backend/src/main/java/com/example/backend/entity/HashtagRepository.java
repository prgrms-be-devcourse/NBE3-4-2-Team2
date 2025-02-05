package com.example.backend.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HashtagRepository extends JpaRepository<HashtagEntity, Long> {

	Optional<HashtagEntity> findByContent(String content);

	/**
	 * hashtag 의 lastUsedAt 을 현재시간으로 한번에 업데이트 하는 벌크성 쿼리입니다
	 * @author kwak
	 * @since 2025-02-03
	 */
	@Modifying(clearAutomatically = true)
	@Query("""
		UPDATE HashtagEntity h
		SET h.lastUsedAt = :now
		WHERE h.id IN :hashtagUsageData
		""")
	void bulkLastUsedAt(@Param("hashtagUsageData") Set<Long> hashtagUsageData, @Param("now") LocalDateTime now);

	/**
	 * ids 기반으로 벌크성 삭제 쿼리
	 * @author kwak
	 * @since 2025-02-03
	 */
	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM HashtagEntity h WHERE h.id IN :ids")
	int bulkDeleteByIds(@Param("ids") List<Long> ids);

	/**
	 * 사용한지 3개월 이상 된 hashtag Id 조회
	 * @author kwak
	 * @since 2025-02-03
	 */
	@Query("SELECT h.id FROM HashtagEntity h WHERE h.lastUsedAt < :date")
	List<Long> findOldHashtags(@Param("targetDate") LocalDateTime targetDate);
}
