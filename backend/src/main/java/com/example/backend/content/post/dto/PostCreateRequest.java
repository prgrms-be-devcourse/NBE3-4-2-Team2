package com.example.backend.content.post.dto;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.PostEntity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시물 생성 관련 DTO
 * 게시물 생성에서 발생하는 요청 관련 DTO
 *
 * @author joonaeng
 * @since 2025-01-31
 */
@Builder
public record PostCreateRequest (
	@NotNull(message = "회원 번호는 필수 입력 값입니다.")	Long memberId,
	@NotNull(message = "게시물 내용은 필수 입력 값입니다.")	String content
){ }
