package com.example.backend.global.event;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.content.notification.exception.NotificationErrorCode;
import com.example.backend.content.notification.exception.NotificationException;
import com.example.backend.content.notification.service.NotificationService;
import com.example.backend.content.notification.type.NotificationType;

/**
 * @author kwak
 * 2025-02-11
 */
@ExtendWith(MockitoExtension.class)
class AllEventListenerTest {

	@Mock
	NotificationService notificationService;

	@InjectMocks
	LikeEventListener likeEventListener;

	@InjectMocks
	FollowEventListener followEventListener;

	@InjectMocks
	CommentEventListener commentEventListener;

	@Test
	@DisplayName("like 이벤트 정상 처리")
	void test1() {
		// given
		LikeEvent likeEvent = getLikeEvent();

		// when
		likeEventListener.handleLikeEvent(likeEvent);

		// then
		verify(notificationService, times(1))
			.createAndSendNotification(1L, 1L, NotificationType.LIKE, "A님이 게시물을 좋아합니다.");
	}

	@Test
	@DisplayName("예외 시 3회 까지 재시도 후 에러 발생")
	void test2() {
		// given
		LikeEvent likeEvent = getLikeEvent();

		doThrow(new NotificationException(NotificationErrorCode.FAILED_SEND))
			.when(notificationService)
			.createAndSendNotification(anyLong(), anyLong(), any(), anyString());

		// when
		// then
		assertThatThrownBy(() -> likeEventListener.handleLikeEvent(likeEvent))
			.isInstanceOf(NotificationException.class)
			.hasMessageContaining("알림 전송에 실패하였습니다.");

		verify(notificationService, times(3))
			.createAndSendNotification(anyLong(), anyLong(), any(), anyString());
	}

	@Test
	@DisplayName("인터럽트 발생시 FAILED_SEND 발생")
	void test3() {
		// given
		LikeEvent likeEvent = getLikeEvent();

		doAnswer(invocationOnMock -> {
			Thread.currentThread().interrupt();
			throw new NotificationException(NotificationErrorCode.FAILED_SEND);
		}).when(notificationService).createAndSendNotification(anyLong(), anyLong(), any(), anyString());

		// when
		// then
		assertThatThrownBy(() -> likeEventListener.handleLikeEvent(likeEvent))
			.isInstanceOf(NotificationException.class)
			.hasMessageContaining("알림 전송에 실패하였습니다.");

		verify(notificationService, times(1))
			.createAndSendNotification(1L, 1L, NotificationType.LIKE, "A님이 게시물을 좋아합니다.");
	}

	@Test
	@DisplayName("follow 이벤트 정상 처리")
	void test4() {
		// given
		FollowEvent followEvent = getFollowEvent();

		// when
		followEventListener.handleFollowEvent(followEvent);

		// then
		verify(notificationService, times(1))
			.createAndSendNotification(1L, 1L, NotificationType.FOLLOW, "B님이 팔로우 요청을 하였습니다.");
	}

	@Test
	@DisplayName("comment 이벤트 정상 처리")
	void test5() {
		// given
		CommentEvent commentEvent = getCommentEvent();

		// when
		commentEventListener.handleCommentEvent(commentEvent);

		// then
		verify(notificationService, times(1))
			.createAndSendNotification(1L, 1L, NotificationType.COMMENT, "C님이 댓글을 달았습니다.");
	}

	private LikeEvent getLikeEvent() {
		return LikeEvent.create("A", 1L, 1L, "POST");
	}

	private FollowEvent getFollowEvent() {
		return FollowEvent.create("B", 1L, 1L);
	}

	private CommentEvent getCommentEvent() {
		return CommentEvent.create("C", 1L, 1L);
	}
}
