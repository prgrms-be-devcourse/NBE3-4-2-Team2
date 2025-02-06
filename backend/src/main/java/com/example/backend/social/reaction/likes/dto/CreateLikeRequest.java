package com.example.backend.social.reaction.likes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 좋아요 적용 Request DTO
 * "/likes" 로 들어오는 생성 요청 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
public record CreateLikeRequest(
	@NotNull(message = "Member Id는 필수 항목입니다.")Long memberId,
	@NotNull(message = "Post Id는 필수 항목입니다.") Long postId
) { }

