package com.example.backend.entity;

import com.example.backend.content.notification.type.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "notification")
public class NotificationEntity extends BaseEntity {
	@Column(nullable = false)
	private String content;

	private Long memberId;

	@Column(nullable = false)
	private NotificationType type;

	@Column(nullable = false)
	private boolean isRead;

	// type 따라 유동적으로 처리되는 targetId
	// LIKE -> postId, Comment -> commentId, Follow -> followId
	private Long targetId;

	public void markRead() {
		this.isRead = true;
	}

	public static NotificationEntity create(
		String message, Long memberId, NotificationType type, Long targetId
	) {
		return NotificationEntity.builder()
			.content(message)
			.memberId(memberId)
			.type(type)
			.targetId(targetId)
			.build();
	}
}
