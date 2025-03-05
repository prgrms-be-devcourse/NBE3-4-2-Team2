package com.example.backend.content.image.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.entity.ImageEntity;
import com.example.backend.entity.ImageRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.global.storage.LocalFileStorageService;

@Service
public class ImageService {

	private final ImageRepository imageRepository;
	private final PostRepository postRepository;
	private final LocalFileStorageService fileStorageService;

	@Autowired
	public ImageService(ImageRepository imageRepository, PostRepository postRepository, LocalFileStorageService fileStorageService) {
		this.imageRepository = imageRepository;
		this.postRepository = postRepository;
		this.fileStorageService = fileStorageService;
	}

	/**
	 * 게시물에 이미지 업로드 및 추가
	 *
	 * @param post 게시물 엔티티
	 * @param files 업로드할 이미지 파일 목록
	 * @return 업로드된 이미지 URL 목록
	 */
	@Transactional
	public List<String> uploadImages(PostEntity post, List<MultipartFile> files) {

		List<String> uploadedUrls = new ArrayList<>();

		// 각 파일을 업로드하고, URL을 리스트에 추가
		for (MultipartFile file : files) {
			String fileUrl = fileStorageService.uploadFile(file);

			// 이미지 엔티티 생성 및 저장
			ImageEntity imageEntity = ImageEntity.create(fileUrl, post);
			imageRepository.save(imageEntity);

			// 게시물에 이미지 추가
			post.addImage(imageEntity);

			// 업로드된 이미지 URL 추가
			uploadedUrls.add(fileUrl);
		}

		return uploadedUrls;
	}

	/**
	 * 게시물에서 이미지 삭제
	 *
	 * @param postId 게시물 ID
	 * @param imageId 이미지 ID
	 */
	@Transactional
	public void deleteImage(Long postId, Long imageId) {
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
	}
}
