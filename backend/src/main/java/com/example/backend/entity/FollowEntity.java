package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
public class FollowEntity extends BaseEntity {
	// follow 요청 시에 sender가 receiver에게 follow 한다.

	@Column(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private MemberEntity sender;

	@Column(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private MemberEntity receiver;
}
