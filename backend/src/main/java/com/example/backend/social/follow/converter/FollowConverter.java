package com.example.backend.social.follow.converter;

import java.time.LocalDateTime;

import com.example.backend.entity.FollowEntity;
import com.example.backend.social.follow.dto.CreateFollowResponse;
import com.example.backend.social.follow.dto.DeleteFollowResponse;

public class FollowConverter {
	/**
	 * 팔로우 응답 DTO 변환 메서드
	 * FollowEntity 객체를 CreateFollowResponse DTO 변환
	 *
	 * @param follow (FollowEntity)
	 * @return CreateFollowResponse
	 */
	public static CreateFollowResponse toCreateResponse(FollowEntity follow) {
		return new CreateFollowResponse(
			follow.getId(),
			follow.getSender().getId(),
			follow.getReceiver().getId(),
			follow.getCreateDate()
		);
	}

	/**
	 /**
	 * 팔로우 취소 응답 DTO 변환 메서드
	 * FollowEntity 객체를 DeleteFollowResponse DTO 변환
	 *
	 * @param follow (FollowEntity)
	 * @return DeleteFollowResponse
	 */
	public static DeleteFollowResponse toDeleteResponse(FollowEntity follow) {
		return new DeleteFollowResponse(
			follow.getId(),
			follow.getSender().getId(),
			follow.getReceiver().getId(),
			LocalDateTime.now()
		);
	}
}
