package com.example.backend.entity;

import java.util.List;
import java.util.Optional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
	/**
	 * 특정 게시글에 속한 모든 댓글을 refOrder 기준으로 정렬하여 가져오기 (트리 구조 유지)
	 */
	@Query("SELECT c FROM CommentEntity c WHERE c.post.id = :postId AND c.isDeleted = false ORDER BY c.refOrder")
	List<CommentEntity> findAllByPostIdAndIsDeletedFalseOrderByRefOrder(@Param("postId") Long postId);

	/**
	 * 특정 ref(최상위 댓글 그룹)에 속한 댓글을 step과 refOrder 기준으로 정렬하여 가져오기
	 */
	List<CommentEntity> findAllByRefOrderByStepAscRefOrderAsc(Long ref);

	/**
	 * 특정 부모 댓글 아래의 모든 대댓글 가져오기 (ref 기준)
	 */
	List<CommentEntity> findAllByParentNum(Long parentNum);

	/**
	 * 게시물 내에서 현재 최대 refOrder 찾기 (새로운 댓글 순서 계산)
	 */
	@Query("SELECT COALESCE(MAX(c.refOrder), 0) FROM CommentEntity c WHERE c.post.id = :postId")
	int findMaxRefOrderByPostId(Long postId);

	// 같은 그룹(ref) 내에서 특정 refOrder 이상인 댓글의 refOrder 증가 (대댓글 위치 조정)
	@Modifying
	@Query("UPDATE CommentEntity c SET c.refOrder = c.refOrder + 1 " +
		"WHERE c.ref = :ref AND c.refOrder >= :newRefOrder")
	void shiftRefOrderWithinGroup(@Param("ref") Long ref, @Param("newRefOrder") int newRefOrder);


	// ✅ 현재 게시물에서 가장 큰 ref 값을 가져오기 (최상위 댓글 그룹 번호 찾기)
	@Query("SELECT COALESCE(MAX(c.ref), 0) FROM CommentEntity c WHERE c.post.id = :postId")
	Long findMaxRefByPostId(@Param("postId") Long postId);


	// ✅ 특정 댓글이 부모 댓글인지 확인 (대댓글이 존재하는지 여부)
	@Query("SELECT COUNT(c) > 0 FROM CommentEntity c WHERE c.parentNum = :parentNum")
	boolean existsByParentNum(@Param("parentNum") Long parentNum);

	// ✅ Soft Delete 적용: 삭제되지 않은 댓글만 조회
	@Query("SELECT c FROM CommentEntity c WHERE c.id = :id AND c.isDeleted = false")
	Optional<CommentEntity> findActiveById(@Param("id") Long id);

	// ✅ 특정 게시글의 댓글을 페이징하여 조회
	Page<CommentEntity> findByPostIdAndIsDeletedFalseOrderByRefOrder(Long postId, Pageable pageable);

	// ✅ 특정 부모 댓글의 대댓글을 페이징하여 조회
	Page<CommentEntity> findByParentNumAndIsDeletedFalse(Long parentNum, Pageable pageable);
}
