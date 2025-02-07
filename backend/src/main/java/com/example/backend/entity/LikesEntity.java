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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "likes")
public class LikesEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	private LocalDateTime createDate;

	@JoinColumn(nullable = false, name = "post_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private PostEntity post;

	@JoinColumn(nullable = false, name = "member_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private MemberEntity member;

	public LikesEntity(
		MemberEntity member, PostEntity post
	) {
		this.member = member;
		this.post = post;
		this.createDate = LocalDateTime.now();
	}

	// 정적 팩토리 메서드
	public static LikesEntity create(MemberEntity member, PostEntity post) {
		return new LikesEntity(member, post);
	}

	public Long getMemberId() {
		return member.getId();
	}

	public Long getPostId() {
		return post.getId();
	}
}
