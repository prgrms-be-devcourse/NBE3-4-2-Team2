package com.example.backend.content.notification.dto;

import java.util.List;

import lombok.Builder;

/**
 * @author kwak
 * 2025-02-10
 */
@Builder
public record NotificationLikePageResponse(

	List<NotificationLikeResponse> responses,
	Integer totalCount, // 전체 알림 개수
	Integer currentPage, // 현재 페이지
	Integer totalPageCount // 전체 페이지 수 (페이지네이션용)
) {
}
