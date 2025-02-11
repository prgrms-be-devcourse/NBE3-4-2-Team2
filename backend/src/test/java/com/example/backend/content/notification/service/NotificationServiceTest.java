package com.example.backend.content.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.notification.converter.NotificationConverter;
import com.example.backend.content.notification.dto.NotificationLikePageResponse;
import com.example.backend.content.notification.sse.SseConnectionPool;
import com.example.backend.content.notification.type.NotificationType;
import com.example.backend.entity.NotificationEntity;
import com.example.backend.entity.NotificationRepository;

/**
 * @author kwak
 * 2025-02-11
 */
@SpringBootTest
@Transactional
class NotificationServiceTest {
	@Autowired
	NotificationRepository notificationRepository;
	@Autowired
	NotificationService notificationService;
	@Autowired
	NotificationConverter converter;
	@MockitoBean
	SseConnectionPool sseConnectionPool;


	@Test
	@DisplayName("알림 생성 및 전송 성공")
	void test1() {
		// given
		Long memberId = 1L;
		Long targetId = 2L;
		NotificationType type = NotificationType.LIKE;
		String message = "A님이 좋아요를 누르셨습니다.";

		// when
		notificationService.createAndSendNotification(memberId, targetId, type, message);

		// then
		NotificationEntity notification = notificationRepository.findById(1L).get();
		assertThat(notification.getContent()).isEqualTo(message);
		assertThat(notification.getType()).isEqualTo(type);
		assertThat(notification.getMemberId()).isEqualTo(1L);
		assertThat(notification.getTargetId()).isEqualTo(2L);

		verify(sseConnectionPool, times(1))
			.sendNotification(anyLong(), any());

	}

	@Test
	@DisplayName("알림 읽음 처리 성공")
	void test2() {
		// given
		Long memberId = 1L;
		Long targetId = 2L;
		NotificationType type = NotificationType.LIKE;
		String message = "A님이 좋아요를 누르셨습니다.";

		NotificationEntity notification = NotificationEntity.create(message, memberId, type, targetId);
		NotificationEntity newNotification = notificationRepository.save(notification);
		notificationRepository.flush();

		// when
		notificationService.markRead(newNotification.getId(), newNotification.getMemberId());

		// then
		NotificationEntity updatedNotification = notificationRepository.findById(newNotification.getId()).get();
		assertTrue(updatedNotification.isRead());

	}

	@Test
	@DisplayName("알림 목록 조회 성공")
	void getNotificationPage() {
		// given
		Long memberId = 1L;
		Long targetId = 2L;

		NotificationEntity notification1 =
			NotificationEntity.create("알림1", memberId, NotificationType.LIKE, targetId);
		NotificationEntity notification2 =
			NotificationEntity.create("알림2", memberId, NotificationType.FOLLOW, targetId);

		// when
		notificationRepository.save(notification1);
		notificationRepository.save(notification2);
		NotificationLikePageResponse response = notificationService.getNotificationPage(0, memberId);

		// then
		assertThat(response.totalCount()).isEqualTo(2);

	}
}
