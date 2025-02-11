package com.example.backend.content.post.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 게시물 생성 관련 DTO
 * 게시물 생성에서 발생하는 요청 관련 DTO
 *
 * @author joonaeng
 * @since 2025-01-31
 */
@Builder
public record PostCreateRequest (
	@NotNull(message = "회원 번호는 필수 입력 값입니다.") Long memberId,
	@NotNull(message = "게시물 내용은 필수 입력 값입니다.") String content,
	List<MultipartFile> images
){ }
