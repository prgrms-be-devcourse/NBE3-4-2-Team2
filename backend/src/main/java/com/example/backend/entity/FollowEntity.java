package com.example.backend.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "follow")
public class FollowEntity{
	// follow 요청 시에 sender가 receiver에게 follow 한다.
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	private LocalDateTime createDate;

	@JoinColumn(nullable = false, name = "sender_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private MemberEntity sender;

	@JoinColumn(nullable = false, name = "receiver_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private MemberEntity receiver;

	public FollowEntity(
		MemberEntity sender, MemberEntity receiver
	) {
		this.sender = sender;
		this.receiver = receiver;
		this.createDate = LocalDateTime.now();
	}

	// 정적 팩토리 메서드
	public static FollowEntity create(MemberEntity sender, MemberEntity receiver) {
		return new FollowEntity(sender, receiver);
	}
}
