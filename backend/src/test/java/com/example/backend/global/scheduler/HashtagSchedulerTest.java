package com.example.backend.global.scheduler;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.content.hashtag.scheduler.HashtagScheduler;

/**
 * 각 비즈니스 로직 단위 테스트만 진행하고
 * 스케줄러가 잘 작동하는지는 로컬에서 직접 확인
 * @author kwak
 * 2025-02-04
 */

@ExtendWith(MockitoExtension.class)
class HashtagSchedulerTest {

	@Mock
	HashtagUsageCollector hashtagUsageCollector;
	@Mock
	PostHashtagService postHashtagService;
	@Mock
	HashtagService hashtagService;
	@InjectMocks
	HashtagScheduler hashtagScheduler;

	@Test
	@DisplayName("해시태그 사용 데이터가 비어있으면 아무것도 안함")
	void dont_update_hashtagUsage_if_empty() {
		// given
		when(hashtagUsageCollector.flushUsageStorage()).thenReturn(Collections.emptySet());

		// when
		hashtagScheduler.updateHashtagUsage();

		// then
		verify(hashtagService, never()).bulkLastUsedAt(anySet(), any());

	}

	@Test
	@DisplayName("updateHashtagUsage 정상 수행")
	void update_hashtagUsage() {
		// given
		Set<Long> hashtagIds = Set.of(1L, 2L, 3L);
		when(hashtagUsageCollector.flushUsageStorage()).thenReturn(hashtagIds);

		// when
		hashtagScheduler.updateHashtagUsage();

		// then
		verify(hashtagService).bulkLastUsedAt(eq(hashtagIds), any(LocalDateTime.class));
	}

	@Test
	@DisplayName("오래된 해시태그 데이터가 비어있으면 아무것도 안함")
	void dont_update_deleteOldHashtag_if_empty() {
		// given
		when(hashtagService.findOldHashtags()).thenReturn(Collections.emptyList());

		// when
		hashtagScheduler.deleteOldHashtag();

		// then
		verify(postHashtagService, never()).deleteByHashtagIds(anyList());
		verify(hashtagService, never()).deleteOldHashtag(anyList());
	}

	@Test
	@DisplayName("오래된 해시태그 삭제 정상 수행")
	void deleteOldHashtag() {
		// given
		List<Long> oldHashtags = List.of(1L, 2L, 3L);
		when(hashtagService.findOldHashtags()).thenReturn(oldHashtags);

		// when
		hashtagScheduler.deleteOldHashtag();

		// then
		verify(postHashtagService).deleteByHashtagIds(eq(oldHashtags));
		verify(hashtagService).deleteOldHashtag(eq(oldHashtags));
	}
}
