package com.example.backend.social.reaction.like.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record LikeInfo(
	Long memberId,
	Long resourceId,
	String resourceType,
	LocalDateTime createDate,
	LocalDateTime modifyDate,
	boolean isActive
) implements Serializable {
	// Java 직렬화를 사용할 때는 serialVersionUID를 추가하는 것이 좋습니다
	private static final long serialVersionUID = 1L;
}
