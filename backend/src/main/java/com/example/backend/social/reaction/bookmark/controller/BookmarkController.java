package com.example.backend.social.reaction.bookmark.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.global.rs.RsData;
import com.example.backend.identity.security.user.SecurityUser;
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

	@PostMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<CreateBookmarkResponse> addBookmarkPost(
		@PathVariable Long postId,
		@AuthenticationPrincipal SecurityUser securityUser
	) {
		CreateBookmarkResponse createResponse = bookmarkService.createBookmark(
			securityUser.getId(), postId
		);
		return RsData.success(createResponse, "북마크가 성공적으로 추가되었습니다.");
	}

	@DeleteMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<DeleteBookmarkResponse> removeBookmarkPost(
		@PathVariable Long postId,
		@Valid @RequestBody DeleteBookmarkRequest deleteRequest,
		@AuthenticationPrincipal SecurityUser securityUser
	) {
		DeleteBookmarkResponse deleteResponse = bookmarkService.deleteBookmark(
			deleteRequest.id(), securityUser.getId(), postId
		);
		return RsData.success(deleteResponse, "북마크가 성공적으로 제거되었습니다.");
	}
}

