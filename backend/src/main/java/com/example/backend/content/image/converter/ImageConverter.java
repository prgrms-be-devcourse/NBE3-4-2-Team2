package com.example.backend.content.image.converter;

import java.util.List;
import java.util.stream.Collectors;

import com.example.backend.content.image.dto.ImageUploadResponse;
import com.example.backend.entity.ImageEntity;

public class ImageConverter {

	/**
	 * 이미지 엔티티 리스트를 ImageUploadResponse로 변환
	 *
	 * @param postId 게시물 ID
	 * @param images 업로드된 이미지 엔티티 리스트
	 * @return ImageUploadResponse DTO
	 */
	public static ImageUploadResponse fromEntityToResponse(Long postId, List<ImageEntity> images) {
		List<String> imageUrls = images.stream()
			.map(ImageEntity::getImageUrl)
			.collect(Collectors.toList());

		return new ImageUploadResponse(postId, imageUrls);
	}

}
