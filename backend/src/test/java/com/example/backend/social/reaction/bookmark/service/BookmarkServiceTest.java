package com.example.backend.social.reaction.bookmark.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.BookmarkRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.social.exception.SocialErrorCode;
import com.example.backend.social.exception.SocialException;
import com.example.backend.social.reaction.bookmark.dto.CreateBookmarkResponse;
import com.example.backend.social.reaction.bookmark.dto.DeleteBookmarkResponse;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BookmarkServiceTest {
	@Autowired
	private EntityManager entityManager;

	@Autowired
	private BookmarkService bookmarkService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private BookmarkRepository bookmarkRepository;

	private MemberEntity testMember;
	private PostEntity testPost;
	@Autowired
	private MemberService memberService;

	@BeforeEach
	public void setup() {
		// 테스트 전에 데이터 초기화
		bookmarkRepository.deleteAll();
		postRepository.deleteAll();
		memberRepository.deleteAll();

		// 시퀀스 초기화 (테스트 데이터 재 생성시 아이디 값이 올라가기 때문)
		entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE post ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE bookmark ALTER COLUMN id RESTART WITH 1").executeUpdate();

		// 테스트용 멤버 추가
		testMember = memberService.join("testMember","testPassword","test@gmail.com");

		// 테스트용 게시물 추가
		PostEntity post = PostEntity.builder()
			.content("testContent")
			.member(testMember)
			.build();
		testPost = postRepository.save(post);
	}

	@Test
	@DisplayName("1. 북마크 생성 테스트")
	public void t001() {
		// Given
		Long memberId = testMember.getId();
		Long postId = testPost.getId();

		// When
		CreateBookmarkResponse createResponse = bookmarkService.createBookmark(memberId, postId);

		// Then
		assertNotNull(createResponse);
		assertEquals(memberId, createResponse.memberId());
		assertEquals(postId, createResponse.postId());
	}

	@Test
	@DisplayName("2. 북마크 삭제 테스트")
	public void t002() {
		// Given First
		Long firstMemberId = testMember.getId();
		Long firstPostId = testPost.getId();

		// When First
		CreateBookmarkResponse createResponse = bookmarkService.createBookmark(firstMemberId, firstPostId);

		// Then First
		assertNotNull(createResponse);

		// Given Second
		Long secondMemberId = createResponse.memberId();
		Long secondPostId = createResponse.postId();

		// When Second
		DeleteBookmarkResponse deleteResponse = bookmarkService.deleteBookmark(
			createResponse.bookmarkId(), secondMemberId, secondPostId
		);

		// Then Second
		assertNotNull(deleteResponse);
		assertEquals(firstMemberId, deleteResponse.memberId());
		assertEquals(firstPostId, deleteResponse.postId());
	}

	@Test
	@DisplayName("3. 존재하지 않는 멤버 북마크 등록 테스트")
	public void t003() {
		// Given
		Long nonExistMemberId = 99L;
		Long postId = testPost.getId();

		// When & Then
		SocialException exception = assertThrows(SocialException.class, () -> {
			bookmarkService.createBookmark(nonExistMemberId, postId);
		});
		assertEquals(SocialErrorCode.NOT_FOUND, exception.getErrorCode());
		assertEquals("회원 검증에 실패했습니다.", exception.getMessage());
	}

	@Test
	@DisplayName("4. 존재하지 않는 게시물 북마크 등록 테스트")
	public void t004() {
		// Given
		Long memberId = testMember.getId();
		Long nonExistPostId = 99L;

		// When & Then
		SocialException exception = assertThrows(SocialException.class, () -> {
			bookmarkService.createBookmark(memberId, nonExistPostId);
		});
		assertEquals(SocialErrorCode.NOT_FOUND, exception.getErrorCode());
		assertEquals("게시물 정보를 확인할 수 없습니다.", exception.getMessage());
	}

	@Test
	@DisplayName("5. 북마크 중복 등록 테스트")
	public void t005() {
		// Given First
		Long firstMemberId = testMember.getId();
		Long firstPostId = testPost.getId();

		// When First
		CreateBookmarkResponse createResponse = bookmarkService.createBookmark(firstMemberId, firstPostId);

		// Then First
		assertNotNull(createResponse);

		// Given Second
		Long secondMemberId = createResponse.memberId();
		Long secondPostId = createResponse.postId();

		// When & Then Second
		SocialException exception = assertThrows(SocialException.class, () -> {
			bookmarkService.createBookmark(secondMemberId, secondPostId);
		});
		assertEquals(SocialErrorCode.ALREADY_EXISTS, exception.getErrorCode());
	}

	@Test
	@DisplayName("6. 생성되지 않은 북마크 삭제 테스트")
	public void t006() {
		// Given
		Long nonExistBookmarkId = 1L;
		Long memberId = testMember.getId();
		Long postId = testPost.getId();

		// When & Then
		SocialException exception = assertThrows(SocialException.class, () -> {
			bookmarkService.deleteBookmark(nonExistBookmarkId, memberId, postId);
		});
		assertEquals(SocialErrorCode.NOT_FOUND, exception.getErrorCode());
		assertEquals("북마크가 존재하지 않습니다.", exception.getMessage());
	}

	@Test
	@DisplayName("7. 북마크 삭제시 다른 유저가 요청하는 테스트")
	public void t007() {
		// Given First
		Long firstMemberId = testMember.getId();
		Long firstPostid = testPost.getId();

		// When First
		CreateBookmarkResponse createResponse = bookmarkService.createBookmark(firstMemberId, firstPostid);

		// Then First
		assertNotNull(createResponse);

		// Given Second
		Long bookmarkId = createResponse.bookmarkId();
		Long anotherMemberId = 5L;
		Long secondPostId = createResponse.postId();

		// When & Then Second
		SocialException exception = assertThrows(SocialException.class, () -> {
			bookmarkService.deleteBookmark(bookmarkId, anotherMemberId, secondPostId);
		});
		assertEquals(SocialErrorCode.ACTION_NOT_ALLOWED, exception.getErrorCode());
	}

	@Test
	@DisplayName("8. 다른 게시물 번호의 북마크 삭제를 요청하는 테스트")
	public void t008() {
		// Given First
		Long firstMemberId = testMember.getId();
		Long firstPostid = testPost.getId();

		// When First
		CreateBookmarkResponse createResponse = bookmarkService.createBookmark(firstMemberId, firstPostid);

		// Then First
		assertNotNull(createResponse);

		// Given Second
		Long bookmarkId = createResponse.bookmarkId();
		Long memberId = createResponse.memberId();
		Long anotherPostId = 5L;

		// When & Then Second
		SocialException exception = assertThrows(SocialException.class, () -> {
			bookmarkService.deleteBookmark(bookmarkId, memberId, anotherPostId);
		});
		assertEquals(SocialErrorCode.DATA_MISMATCH, exception.getErrorCode());
	}
}

