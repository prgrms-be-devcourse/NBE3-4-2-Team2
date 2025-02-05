package com.example.backend.social.reaction.bookmark.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.BookmarkEntity;
import com.example.backend.entity.BookmarkRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.social.reaction.bookmark.dto.CreateBookmarkResponse;
import com.example.backend.social.reaction.bookmark.dto.DeleteBookmarkResponse;
import com.example.backend.social.reaction.bookmark.exception.BookmarkErrorCode;
import com.example.backend.social.reaction.bookmark.exception.BookmarkException;

/**
 * 북마크 서비스
 * 북마크 서비스 관련 로직 구현
 *
 * @author Metronon
 * @since 2025-01-31
 */
@Service
public class BookmarkService {
	private final BookmarkRepository bookmarkRepository;
	private final MemberRepository memberRepository;
	private final PostRepository postRepository;

	@Autowired
	public BookmarkService(BookmarkRepository bookmarkRepository, MemberRepository memberRepository, PostRepository postRepository) {
		this.bookmarkRepository = bookmarkRepository;
		this.memberRepository = memberRepository;
		this.postRepository = postRepository;
	}

	/**
	 * 북마크 생성 메서드
	 * memberId와 postId를 받아 BookmarkEntity 생성
	 *
	 * @param memberId, postId
	 * @return CreateBookmarkResponse (DTO)
	 */
	@Transactional
	public CreateBookmarkResponse createBookmark(Long memberId, Long postId) {
		// 1. 멤버가 존재하는지 검증
		MemberEntity member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BookmarkException(BookmarkErrorCode.MEMBER_NOT_FOUND));

		// 2. 게시물이 존재하는지 검증
		PostEntity post = postRepository.findById(postId)
			.orElseThrow(() -> new BookmarkException(BookmarkErrorCode.POST_NOT_FOUND));

		// 3. 이미 등록된 북마크인지 검증
		if (bookmarkRepository.findByMemberIdAndPostId(memberId, postId).isPresent()) {
			throw new BookmarkException(BookmarkErrorCode.ALREADY_BOOKMARKED);
		}

		// 4. id 및 생성 날짜를 포함하기 위해 build
		BookmarkEntity bookmark = new BookmarkEntity(member, post);

		// 생성 로직
		bookmarkRepository.save(bookmark);

		return CreateBookmarkResponse.toResponse(bookmark);
	}

	/**
	 * 북마크 삭제 메서드
	 * id, memberId, postId를 받아 BookmarkEntity 삭제
	 *
	 * @param id, memberId, postId
	 * @return DeleteBookmarkResponse (DTO)
	 */
	@Transactional
	public DeleteBookmarkResponse deleteBookmark(Long id, Long memberId, Long postId) {
		// 1. 북마크가 실제로 존재하는지 검증
		BookmarkEntity bookmark = bookmarkRepository.findById(id)
			.orElseThrow(() -> new BookmarkException(BookmarkErrorCode.BOOKMARK_NOT_FOUND));

		// 2. 북마크의 멤버 ID와 요청한 멤버 ID가 동일한지 검증
		if (!bookmark.getMember().getId().equals(memberId)) {
			throw new BookmarkException(BookmarkErrorCode.MEMBER_MISMATCH);
		}

		// 3. 북마크의 게시물 ID와 요청한 게시물 ID가 동일한지 검증
		if (!bookmark.getPost().getId().equals(postId)) {
			throw new BookmarkException(BookmarkErrorCode.POST_MISMATCH);
		}

		// 삭제 로직
		bookmarkRepository.delete(bookmark);

		return DeleteBookmarkResponse.toResponse(bookmark);
	}
}
