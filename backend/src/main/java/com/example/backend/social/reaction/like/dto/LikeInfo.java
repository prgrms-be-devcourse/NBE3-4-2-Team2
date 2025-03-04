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
	private static final long serialVersionUID = 1L;
}
