package com.example.backend.content.image.dto;

import java.util.List;

import com.example.backend.entity.ImageEntity;

import jakarta.validation.constraints.NotNull;

public record ImageUploadResponse (
	@NotNull(message = "게시물 ID는 필수입니다.") Long postId,
	@NotNull(message = "업로드 된 이미지 목록은 필수입니다.") List<String> images
) { }
