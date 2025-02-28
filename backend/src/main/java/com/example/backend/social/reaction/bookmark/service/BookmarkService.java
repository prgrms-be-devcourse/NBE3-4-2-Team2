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
import com.example.backend.social.exception.SocialErrorCode;
import com.example.backend.social.exception.SocialException;
import com.example.backend.social.reaction.bookmark.converter.BookmarkConverter;
import com.example.backend.social.reaction.bookmark.dto.CreateBookmarkResponse;
import com.example.backend.social.reaction.bookmark.dto.DeleteBookmarkResponse;

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
		// 1. 멤버가 존재하는지 검증하고 엔티티 가져오기
		MemberEntity member = memberRepository.findById(memberId)
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "로그인 정보 확인에 실패했습니다."));

		// 2. 게시물이 존재하는지 검증하고 엔티티 가져오기
		PostEntity post = postRepository.findById(postId)
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "게시물 정보를 확인할 수 없습니다."));

		// 3. 이미 등록된 북마크인지 검증
		if (bookmarkRepository.existsByMemberIdAndPostId(memberId, postId)) {
			throw new SocialException(SocialErrorCode.ALREADY_EXISTS);
		}

		// 4. id 및 생성 날짜를 포함하기 위해 build
		BookmarkEntity bookmark = BookmarkEntity.create(member, post);

		// 5. 생성 로직
		bookmarkRepository.save(bookmark);

		return BookmarkConverter.toCreateResponse(bookmark);
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
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "북마크가 존재하지 않습니다."));

		// 2. 북마크의 멤버 ID와 요청한 멤버 ID가 동일한지 검증
		if (!bookmark.getMemberId().equals(memberId)) {
			throw new SocialException(SocialErrorCode.ACTION_NOT_ALLOWED);
		}

		// 3. 북마크의 게시물 ID와 요청한 게시물 ID가 동일한지 검증
		if (!bookmark.getPostId().equals(postId)) {
			throw new SocialException(SocialErrorCode.DATA_MISMATCH);
		}

		// 4. 삭제 로직
		bookmarkRepository.delete(bookmark);

		return BookmarkConverter.toDeleteResponse(bookmark);
	}
}
