package com.example.backend.content.image.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.content.image.dto.ImageUploadResponse;
import com.example.backend.entity.ImageEntity;
import com.example.backend.entity.ImageRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.global.storage.LocalFileStorageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImageService {

	private final ImageRepository imageRepository;
	private final PostRepository postRepository;
	private final LocalFileStorageService fileStorageService;

	@Autowired
	public ImageService(
		ImageRepository imageRepository,
		PostRepository postRepository,
		LocalFileStorageService fileStorageService
	) {
		this.imageRepository = imageRepository;
		this.postRepository = postRepository;
		this.fileStorageService = fileStorageService;
	}

	/**
	 * 게시물에 이미지 업로드 및 저장
	 *
	 * @param postId 게시물 ID
	 * @param files 업로드할 이미지 파일 목록
	 * @return 업로드된 이미지 URL 및 UUID 목록
	 */
	@Transactional
	public ImageUploadResponse uploadImages(Long postId, List<MultipartFile> files) {
		PostEntity post = postRepository.findById(postId)
			.orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));

		List<String> fileNames = files.stream()
			.map(file -> {
				// 파일 업로드
				String fileName = fileStorageService.uploadFile(file);

				// 이미지 엔티티 생성 및 저장
				ImageEntity imageEntity = ImageEntity.create(fileName, post);
				imageRepository.save(imageEntity);
				return fileName; // 전체 URL 반환
			})
			.collect(Collectors.toList());

		// "Response"를 만들 때는 "/uploads/" 경로까지 붙여서
		// List<String> imageUrlsForResponse = fileNames.stream()
		// 	.map(name -> "/localhost/uploads/" + name)
		// 	.collect(Collectors.toList());

		return new ImageUploadResponse(postId, fileNames);
	}

	/**
	 * 게시물의 이미지 정보 조회
	 *
	 * @param postId 게시물 ID
	 * @return 해당 게시물의 이미지 목록 (UUID, URL 포함)
	 */
	@Transactional
	public ImageUploadResponse getImagesByPostId(Long postId) {
		PostEntity post = postRepository.findById(postId)
			.orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));

		List<String> imageUrls = post.getImages().stream()
			.map(ImageEntity::getImageUrl)
			.collect(Collectors.toList());

		return new ImageUploadResponse(postId, imageUrls);
	}

	/**
	 * 게시물에서 이미지 삭제
	 *
	 * @param postId 게시물 ID
	 * @param imageId 이미지 ID
	 */
	@Transactional
	public void deleteImage(Long postId, Long imageId) {
		PostEntity post = postRepository.findById(postId)
			.orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));

		ImageEntity imageEntity = imageRepository.findById(imageId)
			.orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));

		// 게시물에서 해당 이미지 제거
		post.removeImage(imageEntity);

		// 파일 시스템에서 실제 이미지 삭제
		fileStorageService.deleteFile(imageEntity.getImageUrl());

		// 이미지 엔티티 삭제
		imageRepository.delete(imageEntity);
	}
}
