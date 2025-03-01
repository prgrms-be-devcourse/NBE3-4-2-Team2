package com.example.backend.entity;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
	// 단일 알림 조회
	Optional<NotificationEntity> findByIdAndMemberId(Long id, Long memberId);

	// 추후 인덱스 고려
	@Query("""
		SELECT n FROM NotificationEntity n
		WHERE n.memberId = :memberId
		AND n.createDate >= :thirtyDaysAgo
		""")
	Page<NotificationEntity> findByMemberId(
		@Param("memberId") Long memberId, @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo, Pageable pageable);
}
