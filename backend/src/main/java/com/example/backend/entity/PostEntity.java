package com.example.backend.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "post")
public class PostEntity extends BaseEntity {

	@Lob
	private String content;

	@JoinColumn(nullable = false, name = "member_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private MemberEntity member;

	@OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
	@Builder.Default
	private List<ImageEntity> images = new ArrayList<>();

	/**
	 * 게시물 내용 수정 메소드
	 * 내용 수정 시 더티체킹을 사용하지 않고 별도의 메소드 추가
	 *
	 * @param content 변경할 게시물 내용
	 * @author joonaeng
	 * @since 2025-02-03
	 */
	public void modifyContent(@NotNull String content) {
		this.content = content;
	}

	/**
	 * 게시물 내용 삭제 메소드
	 * 게시물 삭제시 이미지 삭제 로직 추가
	 * Soft Delete를 위해 삭제 여부를 변경하는 메소드 추가
	 *
	 * @param content 변경할 게시물 내용
	 */
	@Column(nullable = false)
	@Builder.Default
	private Boolean isDeleted = false; // true : 삭제, false : 활성

	public void deleteContent() {
		this.isDeleted = true;
		this.images.clear(); // 관련 이미지 수동 삭제
	}

	/**
	 * 이미지 삭제 메소드
	 * 게시물에서 개별 이미지 삭제 로직
	 *
	 * @param image 삭제할 개별 이미지
	 */
	public void removeImage(ImageEntity image) {
		this.images.remove(image);
	}

	/**
	 * 이미지 추가 메소드
	 * 게시물에 이미지 추가 로직
	 *
	 * @param image 추가할 이미지
	 */
	public void addImage(ImageEntity image) {
		this.images.add(image);
	}

}
