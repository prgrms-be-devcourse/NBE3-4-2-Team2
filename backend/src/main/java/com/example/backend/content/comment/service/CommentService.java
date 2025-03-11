package com.example.backend.content.comment.service;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.comment.converter.CommentConverter;
import com.example.backend.content.comment.dto.CommentCreateRequest;
import com.example.backend.content.comment.dto.CommentCreateResponse;
import com.example.backend.content.comment.dto.CommentDeleteResponse;
import com.example.backend.content.comment.dto.CommentModifyRequest;
import com.example.backend.content.comment.dto.CommentModifyResponse;
import com.example.backend.content.comment.dto.CommentResponse;
import com.example.backend.content.comment.exception.CommentErrorCode;
import com.example.backend.content.comment.exception.CommentException;
import com.example.backend.entity.CommentEntity;
import com.example.backend.entity.CommentRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.global.event.CommentEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final MemberRepository memberRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * 댓글 생성 (최상위 댓글 + 대댓글 지원)
	 */
	@Transactional
	public CommentCreateResponse createComment(CommentCreateRequest request) {
		// 회원 및 게시물 존재 여부 확인
		MemberEntity member = memberRepository.findById(request.memberId())
			.orElseThrow(() -> new CommentException(CommentErrorCode.MEMBER_NOT_FOUND));

		PostEntity post = postRepository.findById(request.postId())
			.orElseThrow(() -> new CommentException(CommentErrorCode.POST_NOT_FOUND));

		CommentEntity comment;

		// 최상위 댓글 생성
		if (request.parentNum() == null) {
			Object[] maxValues = commentRepository.findMaxValuesByPostId(post.getId())
				.orElse(new Object[] {0L, 0L}); // 기본값을 제공하여 null 방지

			// maxValues를 안전하게 처리하기 위해 Object[]를 Long[]로 변환 후 사용
			Long maxRef = (maxValues[0] instanceof Long) ? (Long)maxValues[0] : 0L;
			Long newRef = maxRef + 1;

			// 최상위 댓글 생성
			comment = CommentEntity.createParentComment(request.content(), post, member, newRef);
		} else {
			// 대댓글 생성
			CommentEntity parentComment = commentRepository.findById(request.parentNum())
				.orElseThrow(() -> new CommentException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND));

			Long ref = Optional.ofNullable(parentComment.getRef()).orElse(0L);
			int newRefOrder = parentComment.getRefOrder() + 1;

			// 대댓글의 refOrder를 이동
			commentRepository.shiftRefOrderWithinGroup(ref, newRefOrder);

			// 대댓글 생성
			comment = CommentEntity.createChildComment(request.content(), post, member, parentComment, newRefOrder);

			// 부모 댓글의 답글 수 증가
			parentComment.increaseAnswerNum();

			// 부모 댓글 저장 (반영 필수)
			commentRepository.save(parentComment);
		}

		// 댓글 저장
		CommentEntity savedComment = commentRepository.save(comment);

		// 댓글 생성 이벤트 발행
		applicationEventPublisher.publishEvent(
			CommentEvent.create(member.getUsername(), post.getMember().getId(), comment.getId(), request.postId())
		);

		// 저장된 댓글에 대한 응답 반환
		return CommentConverter.toCreateResponse(savedComment);
	}

	/**
	 * 댓글 수정
	 */
	@Transactional
	public CommentModifyResponse modifyComment(Long commentId, CommentModifyRequest request) {
		CommentEntity comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

		// 작성자 확인
		if (!comment.getMember().getId().equals(request.memberId())) {
			throw new CommentException(CommentErrorCode.COMMENT_UPDATE_FORBIDDEN);
		}

		comment.modifyComment(request.content());
		return CommentConverter.toModifyResponse(comment);
	}

	/**
	 * 댓글 삭제
	 */
	@Transactional
	public CommentDeleteResponse deleteComment(Long commentId, Long memberId) {
		// 댓글 조회
		CommentEntity comment = commentRepository.findById(commentId)
			.filter(c -> !c.isDeleted())  // 삭제된 댓글은 필터링
			.orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

		// 작성자 확인
		if (!comment.getMember().getId().equals(memberId)) {
			throw new CommentException(CommentErrorCode.COMMENT_DELETE_FORBIDDEN);
		}

		boolean hasChildren = commentRepository.existsByParentNum(comment.getId());

		if (hasChildren) {
			// 자식 댓글이 있을 경우 Soft Delete (내용 변경)
			comment.deleteComment();
		} else {
			commentRepository.delete(comment);
			Optional.ofNullable(comment.getParentNum())
				.flatMap(commentRepository::findById) // Optional<CommentEntity>로 반환
				.filter(parent -> !commentRepository.existsByParentNum(parent.getId()) && parent.isDeleted())
				.ifPresent(commentRepository::delete);
		}

		return CommentConverter.toDeleteResponse(comment.getId(), comment.getMember().getId());
	}

	/**
	 * 댓글 단건 조회
	 */
	@Transactional(readOnly = true)
	public CommentResponse findCommentById(Long commentId) {
		return CommentConverter.toResponse(
			commentRepository.findActiveById(commentId)
				.orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND))
		);
	}

	/**
	 * ✅ 특정 게시글의 댓글 목록을 트리 구조 유지하면서 페이징 조회
	 */
	@Transactional(readOnly = true)
	public Page<CommentResponse> findAllCommentsByPostId(Long postId, Pageable pageable) {
		Page<CommentEntity> comments = commentRepository.findByPostIdAndIsDeletedFalseOrderByRefOrder(postId, pageable);
		return comments.map(CommentConverter::toResponse);
	}

	/**
	 * ✅ 특정 댓글의 대댓글을 트리 구조 유지하면서 페이징 조회
	 */
	@Transactional(readOnly = true)
	public Page<CommentResponse> findRepliesByParentId(Long parentId, Pageable pageable) {
		Page<CommentEntity> replies = commentRepository.findByParentNumAndIsDeletedFalse(parentId, pageable);
		return replies.map(CommentConverter::toResponse);
	}
}
