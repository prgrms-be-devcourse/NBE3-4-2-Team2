package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

	@Column(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	PostEntity postEntity;

	@Column(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	MemberEntity memberEntity;
}
