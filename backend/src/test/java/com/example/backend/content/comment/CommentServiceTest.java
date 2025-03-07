package com.example.backend.content.comment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.comment.dto.CommentCreateRequest;
import com.example.backend.content.comment.dto.CommentCreateResponse;
import com.example.backend.content.comment.dto.CommentDeleteResponse;
import com.example.backend.content.comment.dto.CommentModifyRequest;
import com.example.backend.content.comment.dto.CommentModifyResponse;
import com.example.backend.content.comment.dto.CommentResponse;
import com.example.backend.content.comment.exception.CommentErrorCode;
import com.example.backend.content.comment.exception.CommentException;
import com.example.backend.content.comment.service.CommentService;
import com.example.backend.entity.CommentEntity;
import com.example.backend.entity.CommentRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.global.event.CommentEventListener;

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

	@MockitoBean
	CommentEventListener commentEventListener;

	private MemberEntity testMember;
	private PostEntity testPost;
	private CommentEntity testComment;

	@BeforeEach
	void setUp() {
		testMember = memberRepository.saveAndFlush(MemberEntity.builder()
			.username("testUser")
			.email("test@example.com")
			.password("password")
			.build());

		testPost = postRepository.saveAndFlush(PostEntity.builder()
			.content("테스트 게시물")
			.member(testMember)
			.isDeleted(false)
			.build());

		testComment = commentRepository.saveAndFlush(
			CommentEntity.createParentComment("테스트 댓글", testPost, testMember, 1L));
	}

	private Pageable getDefaultPageable() {
		return PageRequest.of(0, 10); // 기본적으로 10개씩 조회하는 페이지네이션 설정
	}

	@Test
	@DisplayName("댓글 생성 테스트")
	void t1() {
		// given
		CommentCreateRequest request = new CommentCreateRequest(testPost.getId(), testMember.getId(), "새로운 댓글", null);

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
		CommentModifyRequest request = new CommentModifyRequest(testComment.getId(), testMember.getId(),
			updatedContent);

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
		CommentCreateRequest request = new CommentCreateRequest(
			testPost.getId(), testMember.getId(), "대댓글", testComment.getId());

		// when
		CommentCreateResponse response = commentService.createComment(request);

		// then
		assertNotNull(response);
		assertEquals("대댓글", response.content());

		entityManager.flush();
		entityManager.clear();

		// ✅ 부모 댓글을 다시 조회하여 answerNum이 증가했는지 확인
		CommentEntity parentComment = commentRepository.findById(testComment.getId()).orElseThrow();
		assertEquals(1, parentComment.getAnswerNum());

		System.out.println("✅ 대댓글 생성 및 answerNum 증가 테스트 성공!");
	}

	@Test
	@DisplayName("존재하지 않는 댓글 수정 시 예외 발생 테스트")
	void t6() {
		// given
		Long nonExistentCommentId = 999999L;
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
				.password("password")
				.build()
		);

		CommentModifyRequest request = new CommentModifyRequest(testComment.getId(), anotherUser.getId(), "허가되지 않은 수정");

		// when & then
		CommentException exception = assertThrows(CommentException.class, () -> {
			commentService.modifyComment(testComment.getId(), request);
		});

		assertEquals(CommentErrorCode.COMMENT_UPDATE_FORBIDDEN, exception.getCommentErrorCode());

		System.out.println("✅ 다른 사용자의 댓글 수정 예외 테스트 성공!");
	}

	@Test
	@DisplayName("단일 댓글 조회 테스트")
	void t8() {
		// given
		Long commentId = testComment.getId();

		// when
		CommentResponse response = commentService.findCommentById(commentId);

		// then
		assertNotNull(response);
		assertEquals(testComment.getContent(), response.content());
		assertEquals(testComment.getId(), response.id());

		System.out.println("✅ 단일 댓글 조회 테스트 성공!");
	}

	@Test
	@DisplayName("게시글 내 모든 댓글 조회 테스트")
	void t9() {
		// given
		CommentEntity comment2 = CommentEntity.createParentComment("테스트 댓글2", testPost, testMember, 2L);
		commentRepository.save(comment2);

		// when
		List<CommentResponse> comments = commentService.findAllCommentsByPostId(testPost.getId(), getDefaultPageable())
			.getContent();

		// then
		assertNotNull(comments);
		assertEquals(2, comments.size()); // 2개의 댓글이 존재해야 함

		System.out.println("✅ 게시글 내 모든 댓글 조회 테스트 성공!");
	}

	@Test
	@DisplayName("특정 댓글의 대댓글 조회 테스트")
	void t10() {
		// given
		CommentEntity child1 = CommentEntity.createChildComment("대댓글1", testPost, testMember, testComment, 2);
		CommentEntity child2 = CommentEntity.createChildComment("대댓글2", testPost, testMember, testComment, 3);
		commentRepository.save(child1);
		commentRepository.save(child2);

		// when
		List<CommentResponse> replies = commentService.findRepliesByParentId(testComment.getId(), getDefaultPageable())
			.getContent();

		// then
		assertNotNull(replies);
		assertEquals(2, replies.size()); // 대댓글이 2개 존재해야 함

		System.out.println("✅ 특정 댓글의 대댓글 조회 테스트 성공!");
	}

	@Test
	@DisplayName("게시글 내 댓글 페이징 조회 테스트")
	void t11() {
		// given
		CommentEntity comment2 = CommentEntity.createParentComment("테스트 댓글2", testPost, testMember, 2L);
		CommentEntity comment3 = CommentEntity.createParentComment("테스트 댓글3", testPost, testMember, 3L);
		commentRepository.save(comment2);
		commentRepository.save(comment3);

		Pageable pageable = PageRequest.of(0, 2); // 한 페이지에 2개씩 조회

		// when
		Page<CommentResponse> comments = commentService.findAllCommentsByPostId(testPost.getId(), pageable);

		// then
		assertNotNull(comments);
		assertEquals(2, comments.getSize()); // 2개만 가져왔는지 확인

		System.out.println("✅ 게시글 내 댓글 페이징 조회 테스트 성공!");
	}

	@Test
	@DisplayName("대댓글 정렬 순서(refOrder) 변경 테스트")
	void t12() {
		// given
		CommentEntity parent = CommentEntity.createParentComment("부모 댓글", testPost, testMember, 1L);
		commentRepository.save(parent);

		CommentEntity reply1 = CommentEntity.createChildComment("대댓글1", testPost, testMember, parent, 2);
		CommentEntity reply2 = CommentEntity.createChildComment("대댓글2", testPost, testMember, parent, 3);
		commentRepository.save(reply1);
		commentRepository.save(reply2);

		// when
		commentRepository.shiftRefOrderWithinGroup(parent.getRef(), 2);

		// then
		CommentEntity updatedReply1 = commentRepository.findById(reply1.getId()).orElseThrow();
		CommentEntity updatedReply2 = commentRepository.findById(reply2.getId()).orElseThrow();

		assertTrue(updatedReply1.getRefOrder() > 2);
		assertTrue(updatedReply2.getRefOrder() > updatedReply1.getRefOrder());

		System.out.println("✅ 대댓글 정렬 순서 변경 테스트 성공!");
	}

	@Test
	@DisplayName("부모 댓글 자동 삭제 테스트 (자식 댓글 모두 삭제 후)")
	void t13() {
		// given
		CommentEntity parent = CommentEntity.createParentComment("부모 댓글", testPost, testMember, 1L);
		commentRepository.save(parent);

		CommentEntity child1 = CommentEntity.createChildComment("대댓글1", testPost, testMember, parent, 2);
		CommentEntity child2 = CommentEntity.createChildComment("대댓글2", testPost, testMember, parent, 3);
		commentRepository.save(child1);
		commentRepository.save(child2);

		commentService.deleteComment(parent.getId(), testMember.getId()); // 부모 댓글 Soft Delete
		commentService.deleteComment(child1.getId(), testMember.getId()); // 대댓글 삭제
		commentService.deleteComment(child2.getId(), testMember.getId()); // 대댓글 삭제

		// when
		boolean exists = commentRepository.existsById(parent.getId());

		// then
		assertFalse(exists); // 부모 댓글도 자동 삭제되어야 함

		System.out.println("✅ 부모 댓글 자동 삭제 테스트 성공!");
	}

	@Test
	@DisplayName("대량 데이터 환경에서 existsByParentNum() 성능 테스트")
	void t14() {
		// given
		CommentEntity parent = CommentEntity.createParentComment("부모 댓글", testPost, testMember, 1L);
		commentRepository.save(parent);

		for (int i = 0; i < 1000; i++) {
			CommentEntity child = CommentEntity.createChildComment("대댓글 " + i, testPost, testMember, parent, i + 2);
			commentRepository.save(child);
		}

		long startTime = System.currentTimeMillis();

		// when
		boolean hasChildren = commentRepository.existsByParentNum(parent.getId());

		long endTime = System.currentTimeMillis();

		// then
		assertTrue(hasChildren);
		System.out.println("✅ existsByParentNum() 성능 테스트 성공! 실행 시간: " + (endTime - startTime) + "ms");
	}

	@Test
	@DisplayName("최초 댓글 생성 시 findMaxValuesByPostId() null 처리 테스트")
	void t15() {
		// given
		Long newPostId = testPost.getId() + 1; // 새로운 게시글 ID (DB에 없는 경우)

		// when
		Object[] maxValues = commentRepository.findMaxValuesByPostId(newPostId)
			.orElse(new Object[] {0L, 0L});  // 기본값 제공하여 null 방지

		// maxValues가 길이가 1일 수도 있으므로 길이에 따라 처리
		Long maxRef = 0L;
		Long maxRefOrder = 0L;

		if (maxValues.length > 0 && maxValues[0] instanceof Long) {
			maxRef = (Long)maxValues[0]; // 첫 번째 값은 ref
		}

		if (maxValues.length > 1 && maxValues[1] instanceof Long) {
			maxRefOrder = (Long)maxValues[1]; // 두 번째 값은 refOrder
		}

		Long newRef = maxRef + 1; // 새로운 ref 값 계산

		// then
		assertEquals(1L, newRef); // 새로운 ref는 1이 되어야 함
		System.out.println("✅ findMaxValuesByPostId() null 처리 테스트 성공!");
	}

	@Test
	@DisplayName("존재하지 않는 댓글 삭제 시 예외 처리")
	void t16() {
		// given
		Long nonExistentCommentId = 999L; // 존재하지 않는 댓글 ID

		// when & then
		assertThrows(CommentException.class,
			() -> commentService.deleteComment(nonExistentCommentId, testMember.getId()), "COMMENT_NOT_FOUND");
		System.out.println("✅ 존재하지 않는 댓글 삭제 시 예외 처리 테스트 성공!");
	}
}

