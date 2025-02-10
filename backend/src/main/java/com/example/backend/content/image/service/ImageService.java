package com.example.backend.content.image.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.ImageEntity;
import com.example.backend.entity.ImageRepository;
import com.example.backend.entity.PostEntity;

import lombok.RequiredArgsConstructor;

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

	/**
	 * 이미지 엔티티 저장
	 * @param post 게시물 엔티티
	 * @param imageEntities 저장할 이미지 목록
	 * @return 저장된 이미지 엔티티 리스트
	 */
	@Transactional
	public List<ImageEntity> uploadImages(PostEntity post, List<ImageEntity> imageEntities) {
		imageEntities.forEach(image -> post.addImage(image));  // 게시물과 연결
		return imageRepository.saveAll(imageEntities);
	}

	/**
	 * 게시물과 연관된 모든 이미지 삭제
	 * @param post 게시물 엔티티
	 */
	@Transactional
	public void deleteImages(PostEntity post) {
		List<ImageEntity> images = post.getImages();
		imageRepository.deleteAll(images);
	}

}
