package com.example.backend.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
	// 단일 알림 조회
	Optional<NotificationEntity> findByIdAndMemberId(Long id, Long memberId);
}
