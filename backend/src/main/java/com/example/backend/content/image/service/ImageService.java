package com.example.backend.content.image.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.entity.ImageEntity;
import com.example.backend.entity.ImageRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.global.storage.LocalFileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이미지 관련 Service
 *
 * @author joonaeng
 * @since 2025-02-10
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {
	private final ImageRepository imageRepository;
	private final LocalFileStorageService fileStorageService;

	/**
	 * 이미지 엔티티 저장
	 * @param post 게시물 엔티티
	 * @param images 저장할 이미지 목록
	 * @return 저장된 이미지 엔티티 리스트
	 */
	@Transactional
	public List<ImageEntity> uploadImages(PostEntity post, List<MultipartFile> images) {

		if (images == null || images.isEmpty()) {
			return Collections.emptyList();
		}

		return images.stream()
				.map(file -> {
					//서버(NGINX)에 파일 업로드 후 URL 반환
					String imageUrl = fileStorageService.uploadFile(file);
					//DB에 저장할 ImageEntity 생성
					return ImageEntity.create(imageUrl, post);
				})
			.map(imageRepository::save)
			.collect(Collectors.toList());
	}

	/**
	 * 게시물과 연관된 모든 이미지 삭제
	 * @param post 게시물 엔티티
	 */
	@Transactional
	public void deleteImages(PostEntity post) {
		List<ImageEntity> images = post.getImages();
		images.forEach(image -> fileStorageService.deleteFile(image.getImageUrl()));
		imageRepository.deleteAll(images);
	}

}
