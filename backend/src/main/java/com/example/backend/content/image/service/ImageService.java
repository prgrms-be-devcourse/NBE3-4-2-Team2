package com.example.backend.content.image.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
	 * @param post 게시물 엔티티
	 * @param files 업로드할 이미지 파일 목록
	 * @return 업로드된 이미지 URL 목록
	 */
	@Transactional
	public List<String> uploadImages(PostEntity post, List<MultipartFile> files) {
		return files.stream()
			.map(file -> {
				// 파일 업로드
				String fileUrl = fileStorageService.uploadFile(file);

				// 이미지 엔티티 생성 및 저장
				ImageEntity imageEntity = ImageEntity.create(fileUrl, post);
				imageRepository.save(imageEntity);

				// 게시물에 이미지 추가
				post.addImage(imageEntity);

				return extractImageId(fileUrl);
			})
			.collect(Collectors.toList());
	}

	/**
	 * 게시물에서 이미지 삭제
	 *
	 * @param postId 게시물 ID
	 * @param imageId 이미지 ID
	 */
	@Transactional
	public void deleteImage(Long postId, Long imageId) {
		try {
			// 해당 게시물 조회
			PostEntity post = postRepository.findById(postId)
				.orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));

			// 삭제할 이미지 엔티티 조회
			ImageEntity imageEntity = imageRepository.findById(imageId)
				.orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));

			// 게시물에서 해당 이미지 제거
			post.removeImage(imageEntity);

			// 파일 시스템에서 실제 이미지 삭제
			fileStorageService.deleteFile(imageEntity.getImageUrl());

			// 이미지 엔티티 삭제
			imageRepository.delete(imageEntity);

			log.info("게시물 {} - 이미지 {} 삭제 완료", postId, imageId);
		} catch (Exception e) {
			log.error("이미지 삭제 중 오류 발생: {}", e.getMessage());
			throw new RuntimeException("이미지 삭제 실패", e);
		}
	}

	/**
	 * 이미지 URL 추출 메서드
	 *
	 * @param fullUrl 전체 파일 URL
	 * @return 이미지 식별자
	 */
	public String extractImageId(String fullUrl) {
		String filename = fullUrl.replace("/uploads/","");
		return filename.substring(0,filename.lastIndexOf("."));
	}
}