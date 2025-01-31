package com.example.backend.social.reaction.bookmark.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.BookmarkEntity;
import com.example.backend.entity.BookmarkRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
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
	 * @param memberId
	 * @param postId
	 * @return BookmarkEntity
	 */
	@Transactional
	public BookmarkEntity createBookmark(Long memberId, Long postId) {
		MemberEntity member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BookmarkException(BookmarkErrorCode.MEMBER_NOT_FOUND));
		PostEntity post = postRepository.findById(postId)
			.orElseThrow(() -> new BookmarkException(BookmarkErrorCode.POST_NOT_FOUND));

		BookmarkEntity bookmark = new BookmarkEntity(member, post, LocalDateTime.now());
		return bookmarkRepository.save(bookmark);
	}

	/**
	 * 북마크 삭제 메서드
	 * memberId와 postId를 받아 BookmarkEntity 삭제
	 *
	 * @param memberId
	 * @param postId
	 */
	@Transactional
	public void deleteBookmark(Long memberId, Long postId) {
		BookmarkEntity bookmark = bookmarkRepository.findByMemberIdAndPostId(memberId, postId)
			.orElseThrow(() -> new BookmarkException(BookmarkErrorCode.BOOKMARK_NOT_FOUND));
		bookmarkRepository.deleteByMemberIdAndPostId(memberId, postId);
	}
}
