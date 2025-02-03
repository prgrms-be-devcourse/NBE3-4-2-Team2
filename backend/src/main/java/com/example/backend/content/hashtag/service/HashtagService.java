package com.example.backend.content.hashtag.service;

import org.springframework.stereotype.Service;

import com.example.backend.content.hashtag.exception.HashtagErrorCode;
import com.example.backend.content.hashtag.exception.HashtagException;
import com.example.backend.entity.HashtagEntity;
import com.example.backend.entity.HashtagRepository;

import lombok.RequiredArgsConstructor;
/**
 * @author kwak
 * @since 2025-02-03
 */
@Service
@RequiredArgsConstructor
public class HashtagService {

	private final HashtagRepository hashtagRepository;
	private final HashtagUsageCollector collector;

	public HashtagEntity createIfNotExists(String content) {
		HashtagEntity hashtag = hashtagRepository.findByContent(content)
			.orElseGet(() -> hashtagRepository.save(
				HashtagEntity.builder()
					.content(content)
					.build()
			));
		collector.addUsageStorage(hashtag.getId());
		return hashtag;
	}

	public HashtagEntity findByContent(String content) {
		return hashtagRepository.findByContent(content)
			.orElseThrow(() -> new HashtagException(HashtagErrorCode.NOT_FOUND));
	}

}
