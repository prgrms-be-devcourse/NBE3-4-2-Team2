package com.example.backend.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.example.backend.social.reaction.bookmark.dto.BookmarkResponse;

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
@Table(name = "bookmark")
public class BookmarkEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	private LocalDateTime createDate;

	@JoinColumn(nullable = false, name = "post_id")
	@ManyToOne(fetch = FetchType.LAZY)
	PostEntity post;

	@JoinColumn(nullable = false, name = "member_id")
	@ManyToOne(fetch = FetchType.LAZY)
	MemberEntity member;

	public BookmarkEntity(MemberEntity member, PostEntity post, LocalDateTime now) {
		this.member = member;
		this.post = post;
		this.createDate = now;
	}

	/**
	 * 북마크 DTO 변환 메서드
	 * BookmarkEntity 객체를 BookmarkResponse DTO 변환
	 *
	 * @param bookmark (변환할 BookmarkEntity 객체)
	 *
	 * @return BookmarkResponse
	 */
	public BookmarkResponse from(BookmarkEntity bookmark) {
		return BookmarkResponse.builder()
			.id(this.id)
			.memberId(this.member.getId())
			.postId(this.post.getId())
			.createDate(this.createDate)
			.build();
	}
}
