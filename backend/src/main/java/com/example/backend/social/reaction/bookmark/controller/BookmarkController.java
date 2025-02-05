package com.example.backend.social.reaction.bookmark.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.global.rs.RsData;
import com.example.backend.social.reaction.bookmark.dto.CreateBookmarkRequest;
import com.example.backend.social.reaction.bookmark.dto.CreateBookmarkResponse;
import com.example.backend.social.reaction.bookmark.dto.DeleteBookmarkRequest;
import com.example.backend.social.reaction.bookmark.dto.DeleteBookmarkResponse;
import com.example.backend.social.reaction.bookmark.service.BookmarkService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * BookmarkController
 * "/bookmark" 로 들어오는 요청 처리 컨트롤러
 *
 * @author Metronon
 * @since 2025-01-31
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api-v1/bookmark", produces = MediaType.APPLICATION_JSON_VALUE)
public class BookmarkController {
	private final BookmarkService bookmarkService;

	@PostMapping
	public ResponseEntity<RsData<CreateBookmarkResponse>> addBookmarkPost(@Valid @RequestBody CreateBookmarkRequest createBookmarkRequest) {
		CreateBookmarkResponse createBookmarkResponse = bookmarkService.createBookmark(createBookmarkRequest.getMemberId(), createBookmarkRequest.getPostId());
		return ResponseEntity.ok()
			.body(
				RsData.success(createBookmarkResponse, "북마크가 성공적으로 추가되었습니다.")
			);
	}

	@DeleteMapping
	public ResponseEntity<RsData<DeleteBookmarkResponse>> removeBookmarkPost(@Valid @RequestBody DeleteBookmarkRequest deleteBookmarkRequest) {
		DeleteBookmarkResponse deleteBookmarkResponse = bookmarkService.deleteBookmark(deleteBookmarkRequest.getId(), deleteBookmarkRequest.getMemberId(), deleteBookmarkRequest.getPostId());
		return ResponseEntity.ok()
			.body(
				RsData.success(deleteBookmarkResponse, "북마크가 성공적으로 제거되었습니다.")
			);
	}
}
