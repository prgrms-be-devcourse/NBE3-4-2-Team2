package com.example.backend.social.feed.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.global.rs.RsData;
import com.example.backend.social.feed.dto.FeedRequest;
import com.example.backend.social.feed.dto.FeedResponse;
import com.example.backend.social.feed.service.FeedService;

import lombok.RequiredArgsConstructor;

/**
 * FeedController
 * "/feed" 로 들어오는 요청 처리 컨트롤러
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api-v1/feed")
public class FeedController {

	private final FeedService feedService;

	@GetMapping
	public ResponseEntity<RsData<List<FeedResponse>>> findList(
		@RequestBody FeedRequest request
	) {
		Long userId = 1L; // 시큐리티 완성되기 전까지 테스트로 사용할 유저이름

		return ResponseEntity.ok()
			.body(
				RsData.success(feedService.findList(request, userId))
			);
	}
}
