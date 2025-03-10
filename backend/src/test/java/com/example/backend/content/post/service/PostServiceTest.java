package com.example.backend.content.post.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import com.example.backend.identity.member.service.MemberService;

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
	private PostEntity testPostWithoutImages;
	private PostEntity testPostWithImages;


	@Autowired
	private MemberService memberService;


	@BeforeEach
	void setUp() {
		testMember = memberService.join("testUser","password","test@example.com");

		testPostWithoutImages = PostEntity.builder()
			.content("이미지 없는 테스트 게시물")
			.member(testMember)
			.isDeleted(false)
			.images(new ArrayList<>())
			.build();
		postRepository.save(testPostWithoutImages);

		List<MultipartFile> images = new ArrayList<>();
		images.add(new MockMultipartFile("file", "image1.jpg", "image/jpeg", "test data".getBytes()));
		images.add(new MockMultipartFile("file", "image2.jpg", "image/jpeg", "test data".getBytes()));

		testPostWithImages = PostEntity.builder()
			.content("이미지 포함 테스트 게시물")
			.member(testMember)
			.isDeleted(false)
			.images(new ArrayList<>()) // 실제로 ImageEntity를 저장하는 로직 필요
			.build();
		postRepository.save(testPostWithImages);
	}

	@Test
	@DisplayName("게시물 생성 테스트 - 이미지 없음")
	void t1_without_images() {
		// given
		PostCreateRequest request = new PostCreateRequest(testMember.getId(), "이미지 없는 게시물", Collections.emptyList());

		// when
		PostCreateResponse response = postService.createPost(request);

		// then
		assertNotNull(response);
		assertEquals("이미지 없는 게시물", response.content());
		assertEquals(testMember.getId(), response.memberId());
		assertTrue(response.imgUrlList().isEmpty(), "이미지 URL 리스트는 빈 리스트여야 함");
	}

	@Test
	@DisplayName("게시물 생성 테스트 - 이미지 포함")
	void t1_with_images() {
		// given
		List<MultipartFile> images = new ArrayList<>();
		images.add(new MockMultipartFile("file", "image1.jpg", "image/jpeg", "test data".getBytes()));
		images.add(new MockMultipartFile("file", "image2.jpg", "image/jpeg", "test data".getBytes()));

		PostCreateRequest request = new PostCreateRequest(testMember.getId(), "이미지 포함 게시물", images);

		// when
		PostCreateResponse response = postService.createPost(request);

		// then
		assertNotNull(response);
		assertEquals("이미지 포함 게시물", response.content());
		assertEquals(testMember.getId(), response.memberId());
		assertFalse(response.imgUrlList().isEmpty(), "이미지 URL 리스트는 빈 리스트가 아니어야 함");
		assertEquals(2, response.imgUrlList().size(), "이미지 URL 리스트의 크기는 2이어야 함");
	}


	@Test
	@DisplayName("게시물 수정 테스트 - 이미지 URL 포함")
	void t2() {
		// given
		String updatedContent = "수정된 게시물 내용";
		PostModifyRequest request = new PostModifyRequest(testPostWithImages.getId(), updatedContent, testMember.getId(), Collections.emptyList());

		// when
		PostModifyResponse response = postService.modifyPost(testPostWithImages.getId(), request);

		// then
		assertNotNull(response);
		assertEquals(updatedContent, response.content()); // 응답 DTO 값 검증

		PostEntity updatedPost = postRepository.findById(testPostWithImages.getId()).orElseThrow();
		assertEquals(updatedContent, updatedPost.getContent()); // 실제 DB 반영 확인

		System.out.println("게시물 수정 성공: " + updatedPost.getContent());
	}

	@Test
	@DisplayName("게시물 삭제 테스트")
	void t3() {
		// given
		Long postId = testPostWithImages.getId();
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
		PostModifyRequest request = new PostModifyRequest(nonExistentPostId, "수정된 내용", testMember.getId(), null);

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
		MemberEntity anotherUser = memberService.join("otherUser", "password", "other@example.com");

		PostModifyRequest request = new PostModifyRequest(testPostWithImages.getId(), "허가되지 않은 수정", anotherUser.getId(), null);

		// when & then
		PostException exception = assertThrows(PostException.class, () -> {
			postService.modifyPost(testPostWithImages.getId(), request);
		});

		assertEquals(PostErrorCode.POST_UPDATE_FORBIDDEN, exception.getPostErrorCode());
		System.out.println("✅ 다른 사용자의 게시물 수정 시 예외 발생 테스트 통과");
	}

	@Test
	@DisplayName("다른 사용자의 게시물 삭제시 예외발생")
	void t7() {
		// given
		MemberEntity anotherUser = memberService.join("otherUser", "password", "other@example.com");

		// when & then
		PostException exception = assertThrows(PostException.class, () -> {
			postService.deletePost(testPostWithImages.getId(), anotherUser.getId());
		});

		assertEquals(PostErrorCode.POST_DELETE_FORBIDDEN, exception.getPostErrorCode());
		System.out.println("✅ 다른 사용자의 게시물 삭제 시 예외 발생 테스트 통과");
	}
}
