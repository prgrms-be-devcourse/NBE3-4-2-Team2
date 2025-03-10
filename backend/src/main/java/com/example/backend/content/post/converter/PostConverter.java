package com.example.backend.content.post.converter;

import java.util.List;

import com.example.backend.content.post.dto.PostCreateResponse;
import com.example.backend.content.post.dto.PostDeleteResponse;
import com.example.backend.content.post.dto.PostModifyResponse;
import com.example.backend.entity.PostEntity;

public class PostConverter {

	private PostConverter() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * PostEntity 객체를 PostCreateResponse로 변환
	 *
	 * @param post 게시물 (PostEntity 객체)
	 * @param imgUrlList 이미지 URL 리스트
	 * @return PostCreateResponse 객체
	 */
	public static PostCreateResponse toCreateResponse(PostEntity post, List<String> imgUrlList) {
		return PostCreateResponse.builder()
			.id(post.getId())
			.content(post.getContent())
			.memberId(post.getMember().getId())
			.imgUrlList(imgUrlList) // URL 리스트 추가
			.build();
	}

	/**
	 * 삭제 성공 응답 생성
	 *
	 * @param postId 삭제한 게시물 ID
	 * @return PostDeleteResponse 객체
	 */
	public static PostDeleteResponse toDeleteResponse(Long postId) {
		return PostDeleteResponse.builder()
			.postId(postId)
			.message("게시물 삭제 성공")
			.build();
	}

	/**
	 * PostEntity 객체를 PostModifyResponse로 변환
	 * 수정된 내용을 전달
	 *
	 * @param post 게시물 (PostEntity 객체)
	 * @return PostModifyResponse 객체
	 */
	public static PostModifyResponse toModifyResponse(PostEntity post) {
		return PostModifyResponse.builder()
			.id(post.getId())
			.content(post.getContent())
			.memberId(post.getMember().getId())
			.build();
	}
}

