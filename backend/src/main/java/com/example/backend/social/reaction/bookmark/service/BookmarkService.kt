package com.example.backend.social.reaction.bookmark.service

import com.example.backend.entity.BookmarkEntity
import com.example.backend.entity.BookmarkRepository
import com.example.backend.entity.MemberRepository
import com.example.backend.entity.PostRepository
import com.example.backend.social.exception.SocialErrorCode
import com.example.backend.social.exception.SocialException
import com.example.backend.social.reaction.bookmark.converter.BookmarkConverter
import com.example.backend.social.reaction.bookmark.dto.BookmarkListResponse
import com.example.backend.social.reaction.bookmark.dto.CreateBookmarkResponse
import com.example.backend.social.reaction.bookmark.dto.DeleteBookmarkResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 북마크 서비스
 * 북마크 서비스 관련 로직 구현
 *
 * @author Metronon
 * @since 2025-01-31
 */
@Service
open class BookmarkService @Autowired constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val memberRepository: MemberRepository,
    private val postRepository: PostRepository
) {
    /**
     * 북마크 생성 메서드
     * memberId와 postId를 받아 BookmarkEntity 생성
     *
     * @param memberId, postId
     * @return CreateBookmarkResponse (DTO)
     */
    @Transactional
    open fun createBookmark(memberId: Long, postId: Long): CreateBookmarkResponse {
        // 1. 멤버가 존재하는지 검증하고 엔티티 가져오기
        val member = memberRepository.findById(memberId)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "회원 검증에 실패했습니다.") }

        // 2. 게시물이 존재하는지 검증하고 엔티티 가져오기
        val post = postRepository.findById(postId)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "게시물 정보를 확인할 수 없습니다.") }

        // 3. 이미 등록된 북마크인지 검증
        if (bookmarkRepository.existsByMemberIdAndPostId(memberId, postId)) {
            throw SocialException(SocialErrorCode.ALREADY_EXISTS)
        }

        // 4. id 및 생성 날짜를 포함하기 위해 build
        val bookmark = BookmarkEntity.create(member, post)

        // 5. 생성 로직
        bookmarkRepository.save(bookmark)

        return BookmarkConverter.toCreateResponse(bookmark)
    }

    /**
     * 북마크 삭제 메서드
     * id, memberId, postId를 받아 BookmarkEntity 삭제
     *
     * @param id, memberId, postId
     * @return DeleteBookmarkResponse (DTO)
     */
    @Transactional
    open fun deleteBookmark(id: Long, memberId: Long, postId: Long): DeleteBookmarkResponse {
        // 1. 북마크가 실제로 존재하는지 검증
        val bookmark = bookmarkRepository.findById(id)
            .orElseThrow { SocialException(SocialErrorCode.NOT_FOUND, "북마크가 존재하지 않습니다.") }

        // 2. 북마크의 멤버 ID와 요청한 멤버 ID가 동일한지 검증
        if (bookmark.memberId != memberId) {
            throw SocialException(SocialErrorCode.ACTION_NOT_ALLOWED)
        }

        // 3. 북마크의 게시물 ID와 요청한 게시물 ID가 동일한지 검증
        if (bookmark.postId != postId) {
            throw SocialException(SocialErrorCode.DATA_MISMATCH)
        }

        // 4. 삭제 로직
        bookmarkRepository.delete(bookmark)

        return BookmarkConverter.toDeleteResponse(bookmark)
    }

    /**
     * 회원의 모든 북마크 리스트를 가져오는 메서드
     * memberId를 받아 해당 회원의 북마크 목록 반환
     *
     * @param memberId
     * @return List<BookmarkListResponse> (DTO)
    </BookmarkListResponse> */
    @Transactional(readOnly = true)
    open fun getBookmarkList(memberId: Long): List<BookmarkListResponse> {
        // 1. 멤버가 존재하는지 검증
        if (!memberRepository.existsById(memberId)) {
            throw SocialException(SocialErrorCode.NOT_FOUND, "유저 정보를 확인할 수 없습니다.")
        }

        // 2. 해당 멤버의 북마크 목록 조회
        val bookmarks = bookmarkRepository.findAllByMemberId(memberId)

        // 3. DTO로 변환하여 반환
        return BookmarkConverter.toBookmarkListResponseList(bookmarks)
    }
}
