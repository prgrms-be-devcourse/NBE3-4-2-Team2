package com.example.backend.content.comment.service

import com.example.backend.content.comment.converter.CommentConverter
import com.example.backend.content.comment.dto.CommentCreateRequest
import com.example.backend.content.comment.dto.CommentCreateResponse
import com.example.backend.content.comment.dto.CommentDeleteResponse
import com.example.backend.content.comment.dto.CommentModifyRequest
import com.example.backend.content.comment.dto.CommentModifyResponse
import com.example.backend.content.comment.dto.CommentResponse
import com.example.backend.content.comment.exception.CommentErrorCode
import com.example.backend.content.comment.exception.CommentException
import com.example.backend.entity.CommentEntity
import com.example.backend.entity.CommentRepository
import com.example.backend.entity.MemberRepository
import com.example.backend.entity.PostRepository
import com.example.backend.global.event.CommentEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
open class CommentService(
	private val commentRepository: CommentRepository,
	private val postRepository: PostRepository,
	private val memberRepository: MemberRepository,
	private val applicationEventPublisher: ApplicationEventPublisher
) {

	/**
	 * 댓글 생성 (최상위 댓글 + 대댓글)
	 */
	@Transactional
	open fun createComment(request: CommentCreateRequest): CommentCreateResponse {
		val member = memberRepository.findById(request.memberId())
			.orElseThrow { CommentException(CommentErrorCode.MEMBER_NOT_FOUND) }

		val post = postRepository.findById(request.postId())
			.orElseThrow { CommentException(CommentErrorCode.POST_NOT_FOUND) }

		val comment: CommentEntity
		// 최상위 댓글
		if (request.parentNum() == null) {
			val maxValues = commentRepository.findMaxValuesByPostId(post.getId()!!)
				.orElse(arrayOf(0L, 0L))

			val maxRef = if (maxValues[0] is Long) maxValues[0] as Long else 0L
			val newRef = maxRef + 1

			comment = CommentEntity.createParentComment(request.content(), post, member, newRef)
		} else {
			// 대댓글
			val parentComment = commentRepository.findById(request.parentNum())
				.orElseThrow { CommentException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND) }

			val refVal = parentComment.getRef() ?: 0L
			val newRefOrder = parentComment.getRefOrder() + 1

			// refOrder 이동
			commentRepository.shiftRefOrderWithinGroup(refVal, newRefOrder)

			// 대댓글 생성
			comment = CommentEntity.createChildComment(request.content(), post, member, parentComment, newRefOrder)

			// 부모 댓글의 답글 수 증가
			parentComment.increaseAnswerNum()
			commentRepository.save(parentComment)
		}

		val savedComment = commentRepository.save(comment)

		// 이벤트 발행
		applicationEventPublisher.publishEvent(
			CommentEvent.create(
				member.getUsername(),
				post.getMember().getId(),
				comment.getId(),
				request.postId()
			)
		)

		return CommentConverter.toCreateResponse(savedComment)
	}

	/**
	 * 댓글 수정
	 */
	@Transactional
	open fun modifyComment(commentId: Long, request: CommentModifyRequest): CommentModifyResponse {
		val comment = commentRepository.findById(commentId)
			.orElseThrow { CommentException(CommentErrorCode.COMMENT_NOT_FOUND) }

		// 작성자 확인
		if (comment.getMember().getId() != request.memberId()) {
			throw CommentException(CommentErrorCode.COMMENT_UPDATE_FORBIDDEN)
		}

		// 수정
		comment.modifyComment(request.content())
		return CommentConverter.toModifyResponse(comment)
	}

	/**
	 * 댓글 삭제
	 */
	@Transactional
	open fun deleteComment(commentId: Long, memberId: Long): CommentDeleteResponse {
		// 댓글 조회 (Soft Delete 된 건 제외)
		val comment = commentRepository.findById(commentId)
			.filter { !it.isDeleted() }  // 이미 삭제된 댓글은 필터링
			.orElseThrow { CommentException(CommentErrorCode.COMMENT_NOT_FOUND) }

		// 작성자 확인
		if (comment.getMember().getId() != memberId) {
			throw CommentException(CommentErrorCode.COMMENT_DELETE_FORBIDDEN)
		}

		val hasChildren = commentRepository.existsByParentNum(comment.getId())

		if (hasChildren) {
			// 자식 댓글이 있으면 Soft Delete
			comment.deleteComment()
		} else {
			// 자식 없으면 DB에서 직접 삭제
			commentRepository.delete(comment)

			// 부모 댓글 정리
			val parentNum = comment.getParentNum()
			if (parentNum != null) {
				val parentOpt = commentRepository.findById(parentNum)
				parentOpt
					.filter { parent -> !commentRepository.existsByParentNum(parent.getId()) && parent.isDeleted() }
					.ifPresent { parent -> commentRepository.delete(parent) }
			}
		}

		return CommentConverter.toDeleteResponse(comment.getId(), comment.getMember().getId())
	}

	/**
	 * 댓글 단건 조회
	 */
	@Transactional(readOnly = true)
	open fun findCommentById(commentId: Long): CommentResponse {
		val activeComment = commentRepository.findActiveById(commentId)
			.orElseThrow { CommentException(CommentErrorCode.COMMENT_NOT_FOUND) }

		return CommentConverter.toResponse(activeComment)
	}

	/**
	 * 특정 게시글의 댓글 목록을 트리 구조 + 페이징
	 */
	@Transactional(readOnly = true)
	open fun findAllCommentsByPostId(postId: Long, pageable: Pageable): Page<CommentResponse> {
		val comments = commentRepository.findByPostIdAndIsDeletedFalseOrderByRefOrder(postId, pageable)
		return comments.map { CommentConverter.toResponse(it) }
	}

	/**
	 * 특정 댓글의 대댓글 목록 (트리 구조 + 페이징)
	 */
	@Transactional(readOnly = true)
	open fun findRepliesByParentId(parentId: Long, pageable: Pageable): Page<CommentResponse> {
		val replies = commentRepository.findByParentNumAndIsDeletedFalse(parentId, pageable)
		return replies.map { CommentConverter.toResponse(it) }
	}
}
