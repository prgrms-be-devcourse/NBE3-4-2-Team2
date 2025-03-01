package com.example.backend.social.reaction.like.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record LikeInfo(
	Long memberId,
	Long resourceId, // 각 Entity 고유 ID
	String resourceType,  // post, comment, reply
	LocalDateTime createDate,
	LocalDateTime modifyDate,
	boolean isActive
) implements Serializable {
}
