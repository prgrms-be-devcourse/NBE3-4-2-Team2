package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

	@Column(nullable = false)
	private Boolean isRead;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "member_id")
	private MemberEntity member;
}
