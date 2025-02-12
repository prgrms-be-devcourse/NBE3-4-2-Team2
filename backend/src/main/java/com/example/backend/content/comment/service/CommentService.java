package com.example.backend.content.comment.service;

import java.util.Optional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final MemberRepository memberRepository;

	/**
	 * 댓글 생성
	 */
	@Transactional
	public CommentCreateResponse createComment(CommentCreateRequest request) {
		MemberEntity member = memberRepository.findById(request.memberId())
			.orElseThrow(() -> new CommentException(CommentErrorCode.MEMBER_NOT_FOUND));

		PostEntity post = postRepository.findById(request.postId())
			.orElseThrow(() -> new CommentException(CommentErrorCode.POST_NOT_FOUND));

		CommentEntity comment;

		if (request.parentId() == null) { // 최상위 댓글
			Long newRef = Optional.ofNullable(commentRepository.findMaxRefByPostId(post.getId())).orElse(0L) + 1;
			comment = CommentEntity.createParentComment(request.content(), post, member, newRef);
		} else { // 대댓글
			CommentEntity parentComment = commentRepository.findById(request.parentId())
				.orElseThrow(() -> new CommentException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND));

			Long ref = Optional.ofNullable(parentComment.getRef()).orElse(0L);
			int newRefOrder = parentComment.getRefOrder() + 1;
			commentRepository.shiftRefOrderWithinGroup(ref, newRefOrder);

			comment = CommentEntity.createChildComment(request.content(), post, member, parentComment, newRefOrder);
			parentComment.increaseAnswerNum();
		}

		CommentEntity savedComment = commentRepository.save(comment);
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
		CommentEntity comment = commentRepository.findById(commentId)
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

			// ✅ 부모 댓글 삭제 처리 개선 (flatMap → ifPresent 사용)
			Optional.ofNullable(comment.getParentNum())
				.ifPresent(parentId -> {
					CommentEntity parent = commentRepository.findById(parentId)
						.orElse(null);

					if (parent != null) {
						boolean parentHasChildren = commentRepository.existsByParentNum(parent.getId());
						if (!parentHasChildren && parent.isDeleted()) {
							commentRepository.delete(parent);
						}
					}
				});
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
	 * ✅특정 게시글의 댓글 목록을 트리 구조 유지하면서 페이징 조회
	 */
	@Transactional(readOnly = true)
	public Page<CommentResponse> findAllCommentsByPostId(Long postId, Pageable pageable) {
		Page<CommentEntity> comments = commentRepository.findByPostIdAndIsDeletedFalseOrderByRefOrder(postId, pageable);
		return comments.map(CommentConverter::toResponse);
	}

	/**
	 *  특정 댓글의 대댓글을 트리 구조 유지하면서 페이징 조회
	 */
	@Transactional(readOnly = true)
	public Page<CommentResponse> findRepliesByParentId(Long parentId, Pageable pageable) {
		Page<CommentEntity> replies = commentRepository.findByParentNumAndIsDeletedFalse(parentId, pageable);
		return replies.map(CommentConverter::toResponse);
	}
}
