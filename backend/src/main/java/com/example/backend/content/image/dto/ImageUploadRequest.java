package com.example.backend.content.image.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ImageUploadRequest (
	@NotNull(message = "게시물 ID는 필수입니다.") Long postId,
	@NotNull(message = "업로드 할 이미지 목록은 필수입니다.") List<MultipartFile> images
) { }
