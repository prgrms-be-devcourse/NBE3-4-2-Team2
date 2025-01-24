package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
@Table(name = "comment")
public class CommentEntity extends BaseEntity {
	@Column(nullable = false)
	@Lob
	private String content;

	@JoinColumn(nullable = false, name = "post_id")
	@ManyToOne(fetch = FetchType.LAZY)
	PostEntity post;

	@JoinColumn(nullable = false, name = "member_id")
	@ManyToOne(fetch = FetchType.LAZY)
	MemberEntity member;
}
