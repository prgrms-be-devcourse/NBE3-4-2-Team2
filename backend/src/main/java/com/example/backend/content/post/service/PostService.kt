package com.example.backend.content.post.service

import com.example.backend.content.hashtag.service.HashtagExtractor
import com.example.backend.content.hashtag.service.PostHashtagService
import com.example.backend.content.image.dto.ImageUploadResponse
import com.example.backend.content.image.service.ImageService
import com.example.backend.content.post.converter.PostConverter
import com.example.backend.content.post.dto.PostCreateRequest
import com.example.backend.content.post.dto.PostCreateResponse
import com.example.backend.content.post.dto.PostDeleteResponse
import com.example.backend.content.post.dto.PostModifyRequest
import com.example.backend.content.post.dto.PostModifyResponse
import com.example.backend.content.post.exception.PostErrorCode
import com.example.backend.content.post.exception.PostException
import com.example.backend.entity.MemberRepository
import com.example.backend.entity.PostEntity
import com.example.backend.entity.PostRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class PostService(
	private val postRepository: PostRepository,
	private val memberRepository: MemberRepository,
	private val hashtagExtractor: HashtagExtractor,
	private val postHashtagService: PostHashtagService,
	private val imageService: ImageService
) {

	/**
	 * 게시물 생성
	 */
	@Transactional
	open fun createPost(request: PostCreateRequest): PostCreateResponse {
		val memberEntity = memberRepository.findById(request.memberId())
			.orElseThrow { UsernameNotFoundException("존재하지 않는 회원입니다.") }

		// Java 엔티티 PostEntity.create(...) 메서드 활용
		val postEntity = PostEntity.create(request.content(), memberEntity)
		val savedPost = postRepository.save(postEntity)

		// 이미지 업로드
		val uploadedFileNames = mutableListOf<String>()
		if (!request.images().isNullOrEmpty()) {
			val imgUploadResp: ImageUploadResponse =
				imageService.uploadImages(savedPost.getId()!!, request.images())
			uploadedFileNames.addAll(imgUploadResp.images())
		}

		// 해시태그 추출
		val extractHashtags = hashtagExtractor.extractHashtag(savedPost.getContent())
		postHashtagService.create(savedPost, extractHashtags)

		// 응답 생성
		return PostCreateResponse(
			id = savedPost.getId(),
			content = savedPost.getContent(),
			memberId = memberEntity.getId(),
			imgUrlList = uploadedFileNames
		)
	}

	/**
	 * 게시물 수정
	 */
	@Transactional
	open fun modifyPost(postId: Long, request: PostModifyRequest): PostModifyResponse {
		val postEntity = postRepository.findByIdAndIsDeletedFalse(postId)
			.orElseThrow { PostException(PostErrorCode.POST_NOT_FOUND) }

		// 작성자 확인
		if (postEntity.getMember().getId() != request.memberId()) {
			throw PostException(PostErrorCode.POST_UPDATE_FORBIDDEN)
		}

		// 내용 수정
		postEntity.modifyContent(request.content())

		return PostConverter.toModifyResponse(postEntity)
	}

	/**
	 * 게시물 삭제 (Soft Delete)
	 */
	@Transactional
	open fun deletePost(postId: Long, memberId: Long): PostDeleteResponse {
		val postEntity = postRepository.findByIdAndIsDeletedFalse(postId)
			.orElseThrow { PostException(PostErrorCode.POST_NOT_FOUND) }

		// 작성자 확인
		if (postEntity.getMember().getId() != memberId) {
			throw PostException(PostErrorCode.POST_DELETE_FORBIDDEN)
		}

		// Soft Delete
		postEntity.deleteContent()

		return PostConverter.toDeleteResponse(postId)
	}
}
