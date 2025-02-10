package com.example.backend.content.image.service;

import com.example.backend.entity.ImageEntity;
import com.example.backend.entity.ImageRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.global.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 이미지 관련 Service
 *
 * @author joonaeng
 * @since 2025-02-10
 */

@Service
@RequiredArgsConstructor
public class ImageService {
	private final ImageRepository imageRepository;
	private final FileStorageService fileStorageService;

	/**
	 * 이미지 업로드 후 저장
	 *
	 * @param post 게시물 엔티티
	 * @param images 업로드할 이미지 목록
	 * @return 저장된 이미지 엔티티 리스트
	 */
	public List<ImageEntity> uploadImages(PostEntity post, List<MultipartFile> images) {
		return images.stream()
			.map(file -> {
				String imageUrl = fileStorageService.uploadFile(file);
				return ImageEntity.create(imageUrl, post);
			})
			.map(imageRepository::save)
			.collect(Collectors.toList());
	}

	/**
	 * 게시물과 연관된 모든 이미지 삭제
	 *
	 * @param post 게시물 엔티티
	 */
	public void deleteImages(PostEntity post) {
		List<ImageEntity> images = post.getImages();
		images.forEach(image -> fileStorageService.deleteFile(image.getImageUrl()));
		imageRepository.deleteAll(images);
	}
}
