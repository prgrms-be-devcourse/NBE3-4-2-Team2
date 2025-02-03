package com.example.backend.entity;

import java.time.LocalDateTime;
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
	@Modifying
	@Query("""
		UPDATE HashtagEntity h
		SET h.lastUsedAt = :now
		WHERE h.id IN :hashtagUsageData
		""")
	void bulkLastUsedAt(@Param("hashtagUsageData") Set<Long> hashtagUsageData, @Param("now") LocalDateTime now);
}
