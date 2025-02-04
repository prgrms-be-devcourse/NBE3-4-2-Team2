package com.example.backend.content.hashtag.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.entity.HashtagEntity;
import com.example.backend.entity.HashtagRepository;

@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

	@Mock
	private HashtagRepository hashtagRepository;

	@InjectMocks
	private HashtagService hashtagService;

	@Mock
	private HashtagUsageCollector collector;

	@Test
	@DisplayName("db에 존재하면 저장이 되지 않아야 함")
	void createIfNotExists() {

		// given
		String content = "고양이";
		HashtagEntity existingHashtag = HashtagEntity.builder()
			.content(content)
			.build();
		when(hashtagRepository.findByContent(content)).thenReturn(Optional.of(existingHashtag));

		// when
		HashtagEntity result = hashtagService.createIfNotExists(content);

		// then
		assertNotNull(result);
		assertEquals("고양이", result.getContent());
		verify(hashtagRepository, times(0)).save(any(HashtagEntity.class));

	}

}
