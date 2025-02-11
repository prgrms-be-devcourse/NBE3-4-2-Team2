package com.example.backend.content.post.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.image.service.ImageService;
import com.example.backend.content.post.dto.PostCreateRequest;
import com.example.backend.content.post.dto.PostCreateResponse;
import com.example.backend.content.post.dto.PostDeleteResponse;
import com.example.backend.content.post.dto.PostModifyRequest;
import com.example.backend.content.post.dto.PostModifyResponse;
import com.example.backend.content.post.exception.PostErrorCode;
import com.example.backend.content.post.exception.PostException;
import com.example.backend.entity.ImageRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
class PostServiceTest {

	@Autowired
	private PostService postService;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ImageService imageService;

	@PersistenceContext
	private EntityManager entityManager;

	private MemberEntity testMember;
	private PostEntity testPost;

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
			.images(new ArrayList<>())
			.build();
		postRepository.save(testPost);
	}

	@Test
	@DisplayName("게시물 생성 테스트")
	void t1() {
		//given
		PostCreateRequest request = new PostCreateRequest(testMember.getId(), "테스트 게시물입니다.", Collections.emptyList());

		// when
		PostCreateResponse response = postService.createPost(request);

		// then
		assertNotNull(response); // 응답이 null이 아닌지 확인
		assertEquals("테스트 게시물입니다.", response.content()); // 내용 검증
		assertEquals(testMember.getId(), response.memberId()); // 작성자 검증

		System.out.println("✅ 게시물 생성 테스트 성공!");
	}

	@Test
	@DisplayName("게시물 수정 테스트")
	void t2() {
		// given
		String updatedContent = "수정된 게시물 내용";
		PostModifyRequest request = new PostModifyRequest(testPost.getId(), updatedContent, testMember.getId(),Collections.emptyList());

		// when
		PostModifyResponse response = postService.modifyPost(testPost.getId(), request);

		// then
		assertNotNull(response);
		assertEquals(updatedContent, response.content()); // 응답 DTO 값 검증

		PostEntity updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
		assertEquals(updatedContent, updatedPost.getContent()); // 실제 DB 반영 확인

		System.out.println("게시물 수정 성공: " + updatedPost.getContent());
	}

	@Test
	@DisplayName("게시물 삭제 테스트")
	void t3() {
		// given
		Long postId = testPost.getId();
		Long memberId = testMember.getId();

		// when
		PostDeleteResponse response = postService.deletePost(postId, memberId);

		// then
		assertNotNull(response);
		assertEquals(postId, response.postId()); // 응답 DTO 값 검증

		PostEntity deletedPost = postRepository.findById(postId).orElseThrow();
		assertTrue(deletedPost.getIsDeleted()); // 실제 DB에서 isDeleted 값이 true인지 검증

		System.out.println("게시물 삭제 성공, 삭제 상태: " + deletedPost.getIsDeleted());
	}

	@Test
	@DisplayName("존재하지 않는 게시물 삭제시 예외발생")
	void t4() {
		// given
		Long nonExistentPostId = 999L; // 존재하지 않는 게시물 ID
		Long memberId = testMember.getId();

		// when & then
		PostException exception = assertThrows(PostException.class, () -> {
			postService.deletePost(nonExistentPostId, memberId);
		});

		assertEquals(PostErrorCode.POST_NOT_FOUND, exception.getPostErrorCode());

		System.out.println("예외 발생 성공: " + exception.getPostErrorCode());
	}

	@Test
	@DisplayName("존재하지 않는 게시물 수정시 예외발생")
	void t5() {
		// given
		Long nonExistentPostId = 999L; // 존재하지 않는 게시물 ID
		PostModifyRequest request = new PostModifyRequest(nonExistentPostId, "수정된 내용", testMember.getId(),null);

		// when & then
		PostException exception = assertThrows(PostException.class, () -> {
			postService.modifyPost(nonExistentPostId, request);
		});

		// 예외 코드가 POST_NOT_FOUND인지 확인
		assertEquals(PostErrorCode.POST_NOT_FOUND, exception.getPostErrorCode());

		System.out.println("✅ 존재하지 않는 게시물 수정 시 예외 발생 테스트 통과");
	}

	@Test
	@DisplayName("다른 사용자의 게시물 수정시 예외발생")
	void t6() {
		// given
		MemberEntity anotherUser = memberRepository.save(
			MemberEntity.builder()
				.username("otherUser")
				.email("other@example.com")
				.refreshToken("dummyToken") // ✅ NULL 방지: 빈 문자열 또는 더미 값 추가
				.password("password")
				.build()
		);


		PostModifyRequest request = new PostModifyRequest(testPost.getId(), "허가되지 않은 수정", anotherUser.getId(),null);

		// when & then
		PostException exception = assertThrows(PostException.class, () -> {
			postService.modifyPost(testPost.getId(), request);
		});

		assertEquals(PostErrorCode.POST_UPDATE_FORBIDDEN, exception.getPostErrorCode());
		System.out.println("✅ 다른 사용자의 게시물 수정 시 예외 발생 테스트 통과");
	}

	@Test
	@DisplayName("다른 사용자의 게시물 삭제시 예외발생")
	void t7() {
		// given
		MemberEntity anotherUser = memberRepository.save(
			MemberEntity.builder()
				.username("otherUser")
				.email("other@example.com")
				.refreshToken("dummyToken") // ✅ NULL 방지: 빈 문자열 또는 더미 값 추가
				.password("password")
				.build()
		);

		// when & then
		PostException exception = assertThrows(PostException.class, () -> {
			postService.deletePost(testPost.getId(), anotherUser.getId());
		});

		assertEquals(PostErrorCode.POST_DELETE_FORBIDDEN, exception.getPostErrorCode());
		System.out.println("✅ 다른 사용자의 게시물 삭제 시 예외 발생 테스트 통과");
	}
}
