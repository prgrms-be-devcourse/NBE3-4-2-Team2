package com.example.backend.content.image.controller;

import com.example.backend.content.image.converter.ImageConverter;
import com.example.backend.content.image.dto.ImageUploadRequest;
import com.example.backend.content.image.dto.ImageUploadResponse;
import com.example.backend.content.image.service.ImageService;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 이미지 관련 컨트롤러
 *
 * @author joonaeng
 * @since 2025-02-10
 */
@RestController
@RequestMapping("/api-v1/images")
@RequiredArgsConstructor
public class ImageController {

	private final ImageService imageService;
	private final PostRepository postRepository;

	/**
	 * 이미지 업로드 API
	 *
	 * @param postId 게시물 ID
	 * @param images 업로드할 이미지 파일 리스트
	 * @return 업로드된 이미지 URL 리스트
	 */
	@PostMapping
	public ResponseEntity<ImageUploadResponse> uploadImages(
		@RequestParam Long postId,
		@RequestPart List<MultipartFile> images
	) {
		PostEntity post = postRepository.findById(postId)
			.orElseThrow(() -> new IllegalArgumentException("해당 게시물을 찾을 수 없습니다."));

		var uploadedImages = imageService.uploadImages(post, images);
		return ResponseEntity.ok(ImageConverter.fromEntityToResponse(postId, uploadedImages));
	}

	/**
	 * 게시물에 연결된 모든 이미지 삭제 API
	 *
	 * @param postId 게시물 ID
	 * @return 삭제 완료 메시지
	 */
	@DeleteMapping("/delete/{postId}")
	public ResponseEntity<String> deleteImages(@PathVariable Long postId) {
		PostEntity post = postRepository.findById(postId)
			.orElseThrow(() -> new IllegalArgumentException("해당 게시물을 찾을 수 없습니다."));

		imageService.deleteImages(post);
		return ResponseEntity.ok("이미지 삭제 완료");
	}
}
