package com.example.backend.content.image.service

import com.example.backend.content.image.dto.ImageUploadResponse
import com.example.backend.entity.ImageEntity
import com.example.backend.entity.ImageRepository
import com.example.backend.entity.PostEntity
import com.example.backend.entity.PostRepository
import com.example.backend.global.storage.LocalFileStorageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
open class ImageService(
	private val imageRepository: ImageRepository,
	private val postRepository: PostRepository,
	private val fileStorageService: LocalFileStorageService
) {

	/**
	 * 게시물에 이미지 업로드 및 저장
	 */
	@Transactional
	open fun uploadImages(postId: Long, files: List<MultipartFile>): ImageUploadResponse {
		val post: PostEntity = postRepository.findById(postId)
			.orElseThrow { RuntimeException("게시물을 찾을 수 없습니다.") }

		val fileNames = files.map { file ->
			// 실제 파일 업로드
			val fileName = fileStorageService.uploadFile(file)

			// ImageEntity.create(...) 로 엔티티 생성
			val imageEntity = ImageEntity.create(fileName, post)
			imageRepository.save(imageEntity)

			fileName
		}

		// Response에 파일 이름들 담아서 반환
		return ImageUploadResponse(postId, fileNames)
	}

	/**
	 * 게시물의 이미지 정보 조회
	 */
	@Transactional
	open fun getImagesByPostId(postId: Long): ImageUploadResponse {
		val post: PostEntity = postRepository.findById(postId)
			.orElseThrow { RuntimeException("게시물을 찾을 수 없습니다.") }

		// post.getImages()를 통해 이미지 리스트 접근
		val imageUrls = post.getImages().map { it.getImageUrl() }

		return ImageUploadResponse(postId, imageUrls)
	}

	/**
	 * 게시물에서 이미지 삭제
	 */
	@Transactional
	open fun deleteImage(postId: Long, imageId: Long) {
		val post: PostEntity = postRepository.findById(postId)
			.orElseThrow { RuntimeException("게시물을 찾을 수 없습니다.") }

		val imageEntity: ImageEntity = imageRepository.findById(imageId)
			.orElseThrow { RuntimeException("이미지를 찾을 수 없습니다.") }

		// 게시물 엔티티의 removeImage(...) 메서드 사용
		post.removeImage(imageEntity)

		// 실제 파일 삭제
		fileStorageService.deleteFile(imageEntity.getImageUrl())

		// DB에서 삭제
		imageRepository.delete(imageEntity)
	}
}
