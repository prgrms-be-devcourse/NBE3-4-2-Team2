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

	@Column(nullable = false, length = 500)
	@Lob
	private String content; // 댓글 내용 500자 이하

	@JoinColumn(nullable = false, name = "post_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private PostEntity post;

	@JoinColumn(nullable = false, name = "member_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private MemberEntity member;

	@Column(nullable = false)
	private Long ref = 0L; // 최상위 댓글 그룹 번호 (원댓글 기준)

	@Column(nullable = false)
	private int step; // 댓글의 깊이 (0: 원댓글, 1~N: 대댓글)

	@Column(nullable = false)
	private int refOrder; // 같은 그룹 내 정렬 순서

	@Column(nullable = false)
	private Long answerNum; // 해당 댓글의 대댓글 개수

	@Column(nullable = true)
	private Long parentNum; // 부모 댓글 ID (null : 원댓글)

	@Column(nullable = false)
	private boolean isDeleted; // Soft Delete 여부

	/**
	 * 최상위 댓글 생성 (부모 댓글이 없는 경우)
	 */
	public static CommentEntity createParentComment(String content, PostEntity post, MemberEntity member, Long commentId) {
		return CommentEntity.builder()
			.content(content)
			.post(post)
			.member(member)
			.ref(commentId) // 최상위 댓글이므로 ref는 자기 자신 ID
			.step(0) // 최상위 댓글이므로 step은 0
			.refOrder(1) // 첫 번째 댓글이므로 refOrder은 1
			.answerNum(0L) // 초기 대댓글 개수는 0
			.parentNum(null) // 부모 댓글 없음
			.isDeleted(false) // 삭제되지 않은 상태
			.build();
	}

	/**
	 * 대댓글 생성 (부모 댓글이 있는 경우)
	 */
	public static CommentEntity createChildComment(String content, PostEntity post, MemberEntity member, CommentEntity parentComment, int newRefOrder) {
		return CommentEntity.builder()
			.content(content)
			.post(post)
			.member(member)
			.ref(parentComment.getRef()) // 같은 그룹으로 묶어야 하므로 부모의 ref 유지
			.step(parentComment.getStep() + 1) // 부모 댓글보다 한 단계 더 깊은 댓글
			.refOrder(newRefOrder) // 새로운 refOrder로 설정
			.answerNum(0L) // 대댓글 개수 초기값 0
			.parentNum(parentComment.getId()) // 부모 댓글 ID 설정
			.isDeleted(false) // 삭제되지 않은 상태
			.build();
	}

	public void increaseAnswerNum() {
		this.answerNum++;
	}

	public void modifyComment(String newContent) {
		this.content = newContent;
	}

	public void deleteComment() {
		this.isDeleted = true;
		this.content = "삭제된 댓글입니다.";
	}
}
