package com.example.backend.social.feed.controller;

import static com.example.backend.entity.QMemberEntity.*;

import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.global.rs.RsData;
import com.example.backend.social.feed.dto.FeedListResponse;
import com.example.backend.social.feed.dto.FeedRequest;
import com.example.backend.social.feed.service.FeedService;
import com.querydsl.jpa.impl.JPAQueryFactory;

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

	private final JPAQueryFactory queryFactory;

	@GetMapping
	public ResponseEntity<RsData<FeedListResponse>> findList(
		@RequestBody FeedRequest request
	) {
		Long userId = Objects.requireNonNull(
				queryFactory.selectFrom(memberEntity).where(memberEntity.username.eq(request.getUsername())).fetchOne())
			.getId();

		return ResponseEntity.ok()
			.body(
				RsData.success(feedService.findList(request, userId), "피드를 성공적으로 반환했습니다.")
			);
	}
}
