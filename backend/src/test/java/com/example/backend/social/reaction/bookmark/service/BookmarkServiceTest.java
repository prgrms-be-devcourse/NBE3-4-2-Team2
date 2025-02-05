package com.example.backend.social.reaction.bookmark.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.backend.entity.BookmarkRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.social.reaction.bookmark.dto.CreateBookmarkResponse;
import com.example.backend.social.reaction.bookmark.dto.DeleteBookmarkResponse;
import com.example.backend.social.reaction.bookmark.exception.BookmarkErrorCode;
import com.example.backend.social.reaction.bookmark.exception.BookmarkException;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

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
		MemberEntity member = MemberEntity.builder()
			.username("testMember")
			.email("test@gmail.com")
			.password("testPassword")
			.refreshToken(UUID.randomUUID().toString())
			.build();
		testMember = memberRepository.save(member);

		// 테스트용 게시물 추가
		PostEntity post = PostEntity.builder()
			.content("testContent")
			.member(member)
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
		assertEquals(memberId, createResponse.getMemberId());
		assertEquals(postId, createResponse.getPostId());
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
		Long secondMemberId = createResponse.getMemberId();
		Long secondPostId = createResponse.getPostId();

		// When Second
		DeleteBookmarkResponse deleteResponse = bookmarkService.deleteBookmark(
			createResponse.getId(), secondMemberId, secondPostId
		);

		// Then Second
		assertNotNull(deleteResponse);
		assertEquals(firstMemberId, deleteResponse.getMemberId());
		assertEquals(firstPostId, deleteResponse.getPostId());
	}

	@Test
	@DisplayName("3. 존재하지 않는 멤버 북마크 등록 테스트")
	public void t003() {
		// Given
		Long nonExistMemberId = 99L;
		Long postId = testPost.getId();

		// When & Then
		assertThrows(BookmarkException.class, () -> {
			bookmarkService.createBookmark(nonExistMemberId, postId);
		}, BookmarkErrorCode.MEMBER_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("4. 존재하지 않는 게시물 북마크 등록 테스트")
	public void t004() {
		// Given
		Long memberId = testMember.getId();
		Long nonExistPostId = 99L;

		// When & Then
		assertThrows(BookmarkException.class, () -> {
			bookmarkService.createBookmark(memberId, nonExistPostId);
		}, BookmarkErrorCode.POST_NOT_FOUND.getMessage());
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
		Long secondMemberId = createResponse.getMemberId();
		Long secondPostId = createResponse.getPostId();

		// When & Then Second
		assertThrows(BookmarkException.class, () -> {
			bookmarkService.createBookmark(secondMemberId, secondPostId);
		}, BookmarkErrorCode.ALREADY_BOOKMARKED.getMessage());
	}

	@Test
	@DisplayName("6. 생성되지 않은 북마크 삭제 테스트")
	public void t006() {
		// Given
		Long nonExistBookmarkId = 1L;
		Long memberId = testMember.getId();
		Long postId = testPost.getId();

		// When & Then
		assertThrows(BookmarkException.class, () -> {
			bookmarkService.deleteBookmark(nonExistBookmarkId, memberId, postId);
		}, BookmarkErrorCode.BOOKMARK_NOT_FOUND.getMessage());
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
		Long bookmarkId = createResponse.getId();
		Long anotherMemberId = 5L;
		Long secondPostId = createResponse.getPostId();

		// When & Then Second
		assertThrows(BookmarkException.class, () -> {
			bookmarkService.deleteBookmark(bookmarkId, anotherMemberId, secondPostId);
		}, BookmarkErrorCode.MEMBER_MISMATCH.getMessage());
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
		Long bookmarkId = createResponse.getId();
		Long memberId = createResponse.getMemberId();
		Long anotherPostId = 5L;

		// When & Then Second
		assertThrows(BookmarkException.class, () -> {
			bookmarkService.deleteBookmark(bookmarkId, memberId, anotherPostId);
		}, BookmarkErrorCode.MEMBER_MISMATCH.getMessage());
	}
}
