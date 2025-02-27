package com.example.backend.social.reaction.likes.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record LikeInfo(
	Long memberId,
	Long resourceId,
	String resourceType,  // "POST", "COMMENT", "REPLY"
	LocalDateTime createDate,
	LocalDateTime modifyDate,
	boolean isActive
) implements Serializable {
}
