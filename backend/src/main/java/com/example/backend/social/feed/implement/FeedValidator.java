package com.example.backend.social.feed.implement;

import static com.example.backend.social.feed.constant.FeedConstants.*;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.example.backend.social.feed.dto.FeedRequest;
import com.example.backend.social.feed.exception.FeedErrorCode;
import com.example.backend.social.feed.exception.FeedException;

/***
 * FeedValidator
 * 피드 요청 정보의 유효성을 검사하는 클래스
 * @author ChoiHyunSan
 * @since 2025-02-01
 */
@Component
public class FeedValidator {

	/**
	 * Feed 를 요청하는 경우에 호출하여 Request 정보가 유효한지 확인하는 메서드
	 * @param request Feed 요청 정보
	 */
	public void validateRequest(FeedRequest request) {

		// 1. 요청 시간 ( RequestTime )
		// - 요청시간이 현재 시간보다 큰 경우
		LocalDateTime requestTime = request.timestamp();
		if (requestTime == null || requestTime.isAfter(LocalDateTime.now())) {
			throw new FeedException(FeedErrorCode.WRONG_TIMESTAMP_REQUEST);
		}

		// 2. 요청 최대 개수
		// - 0개 이하의 개수를 요청하는 경우
		// - 20개 초과의 개수를 요청하는 경우
		Integer maxSize = request.maxSize();
		if (maxSize == null || maxSize <= 0 || maxSize > REQUEST_FEED_MAX_SIZE) {
			throw new FeedException(FeedErrorCode.WRONG_MAXSIZE_REQUEST);
		}

	}
}
