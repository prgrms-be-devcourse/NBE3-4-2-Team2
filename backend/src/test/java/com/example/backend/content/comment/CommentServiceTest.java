package com.example.backend.content.comment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.comment.dto.CommentCreateRequest;
import com.example.backend.content.comment.dto.CommentCreateResponse;
import com.example.backend.content.comment.dto.CommentDeleteResponse;
import com.example.backend.content.comment.dto.CommentModifyRequest;
import com.example.backend.content.comment.dto.CommentModifyResponse;
import com.example.backend.content.comment.exception.CommentErrorCode;
import com.example.backend.content.comment.exception.CommentException;
import com.example.backend.content.comment.service.CommentService;
import com.example.backend.entity.CommentEntity;
import com.example.backend.entity.CommentRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
class CommentServiceTest {

	@Autowired
	private CommentService commentService;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private MemberEntity testMember;
	private PostEntity testPost;
	private CommentEntity testComment;

	@BeforeEach
	void setUp() {
		testMember = MemberEntity.builder()
			.username("testUser")
			.email("test@example.com")
			.refreshToken("")
			.password("password")
			.build();
		memberRepository.save(testMember);

		testPost = PostEntity.builder()
			.content("테스트 게시물")
			.member(testMember)
			.isDeleted(false)
			.build();
		postRepository.save(testPost);

		testComment = CommentEntity.createParentComment("테스트 댓글", testPost, testMember, 1L);
		commentRepository.save(testComment);
	}

	@Test
	@DisplayName("댓글 생성 테스트")
	void t1() {
		// given
		CommentCreateRequest request = new CommentCreateRequest(testMember.getId(), testPost.getId(), "새로운 댓글", null);

		// when
		CommentCreateResponse response = commentService.createComment(request);

		// then
		assertNotNull(response);
		assertEquals("새로운 댓글", response.content());
		assertEquals(testMember.getId(), response.memberId());
		assertEquals(testPost.getId(), response.postId());

		System.out.println("✅ 댓글 생성 테스트 성공!");
	}

	@Test
	@DisplayName("댓글 수정 테스트")
	void t2() {
		// given
		String updatedContent = "수정된 댓글 내용";
		CommentModifyRequest request = new CommentModifyRequest(testComment.getId(), testMember.getId(),updatedContent);

		// when
		CommentModifyResponse response = commentService.modifyComment(testComment.getId(), request);

		// then
		assertNotNull(response);
		assertEquals(updatedContent, response.content());

		CommentEntity updatedComment = commentRepository.findById(testComment.getId()).orElseThrow();
		assertEquals(updatedContent, updatedComment.getContent());

		System.out.println("✅ 댓글 수정 테스트 성공!");
	}

	@Test
	@DisplayName("댓글 삭제 테스트 (자식 댓글이 없는 경우)")
	void t3() {
		// given
		Long commentId = testComment.getId();
		Long memberId = testMember.getId();

		// when
		CommentDeleteResponse response = commentService.deleteComment(commentId, memberId);

		// then
		assertNotNull(response);
		assertEquals(commentId, response.id());

		assertFalse(commentRepository.existsById(commentId));

		System.out.println("✅ 댓글 삭제 테스트 성공!");
	}

	@Test
	@DisplayName("댓글 삭제 테스트 (자식 댓글이 있는 경우)")
	void t4() {
		// given
		CommentEntity childComment = CommentEntity.createChildComment("대댓글", testPost, testMember, testComment, 2);
		commentRepository.save(childComment);

		Long commentId = testComment.getId();
		Long memberId = testMember.getId();

		// when
		commentService.deleteComment(commentId, memberId);

		CommentEntity deletedComment = commentRepository.findById(commentId).orElseThrow();

		// ✅ 부모 댓글은 Soft Delete 되어야 함
		assertEquals("삭제된 댓글입니다.", deletedComment.getContent()); // ✅ Soft Delete 되었는지 확인
		assertTrue(deletedComment.isDeleted());

		System.out.println("✅ 댓글 삭제 (soft delete) 테스트 성공!");
	}

	@Test
	@DisplayName("대댓글 생성 및 부모 댓글 answerNum 증가 확인")
	void t5() {
		// given
		CommentCreateRequest request = new CommentCreateRequest(testMember.getId(), testPost.getId(), "대댓글", testComment.getId());

		// when
		CommentCreateResponse response = commentService.createComment(request);

		// then
		assertNotNull(response);
		assertEquals("대댓글", response.content());

		CommentEntity parentComment = commentRepository.findById(testComment.getId()).orElseThrow();
		assertEquals(1, parentComment.getAnswerNum());

		System.out.println("✅ 대댓글 생성 및 answerNum 증가 테스트 성공!");
	}

	@Test
	@DisplayName("존재하지 않는 댓글 수정 시 예외 발생 테스트")
	void t6() {
		// given
		Long nonExistentCommentId = 999L;
		CommentModifyRequest request = new CommentModifyRequest(nonExistentCommentId, testMember.getId(), "수정된 내용");

		// when & then
		CommentException exception = assertThrows(CommentException.class, () -> {
			commentService.modifyComment(nonExistentCommentId, request);
		});

		assertEquals(CommentErrorCode.COMMENT_NOT_FOUND, exception.getCommentErrorCode());

		System.out.println("✅ 존재하지 않는 댓글 수정 예외 테스트 성공!");
	}

	@Test
	@DisplayName("다른 사용자의 댓글 수정 시 예외 발생 테스트")
	void t7() {
		// given
		MemberEntity anotherUser = memberRepository.save(
			MemberEntity.builder()
				.username("otherUser")
				.email("other@example.com")
				.refreshToken(UUID.randomUUID().toString())
				.password("password")
				.build()
		);

		CommentModifyRequest request = new CommentModifyRequest(testComment.getId(), anotherUser.getId(),"허가되지 않은 수정");

		// when & then
		CommentException exception = assertThrows(CommentException.class, () -> {
			commentService.modifyComment(testComment.getId(), request);
		});

		assertEquals(CommentErrorCode.COMMENT_UPDATE_FORBIDDEN, exception.getCommentErrorCode());

		System.out.println("✅ 다른 사용자의 댓글 수정 예외 테스트 성공!");
	}
}

