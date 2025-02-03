package com.example.backend.content.hashtag.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.backend.entity.HashtagEntity;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostHashtagEntity;
import com.example.backend.entity.PostHashtagRepository;

import lombok.RequiredArgsConstructor;
/**
 * hashtag 와 post 의 연결 담당하는 클래스
 * @author kwak
 * @since 2025-02-03
 */
@Service
@RequiredArgsConstructor
public class PostHashtagService {

	private final HashtagService hashtagService;
	private final PostHashtagRepository postHashtagRepository;

	public List<PostHashtagEntity> create(PostEntity post, Set<String> contents) {
		List<HashtagEntity> hashtags = contents.stream()
			.map(hashtagService::createIfNotExists)
			.collect(Collectors.toList());

		List<PostHashtagEntity> postHashtags = hashtags.stream()
			.map(hashtag ->
				PostHashtagEntity.builder()
					.post(post)
					.hashtag(hashtag)
					.build()
			).collect(Collectors.toList());

		return postHashtagRepository.saveAll(postHashtags);

	}

}
