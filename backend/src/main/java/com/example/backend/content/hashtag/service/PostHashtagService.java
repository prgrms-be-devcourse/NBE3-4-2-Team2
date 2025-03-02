package com.example.backend.content.hashtag.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
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

	public void create(PostEntity post, Set<String> contents) {
		List<HashtagEntity> hashtags = contents.stream()
			.map(hashtagService::createIfNotExists)
			.toList();

		postHashtagRepository.bulkInsert(post, hashtags);
	}

	public void deleteByHashtagIds(List<Long> oldHashtagIds) {
		postHashtagRepository.bulkDeleteByHashtagIds(oldHashtagIds);
	}

	public List<PostHashtagEntity> findPostHashtagByPostId(Long postId) {
		return postHashtagRepository.findPostHashtagByPostId(postId);
	}

	public void updatePostHashtags(PostEntity post, Set<String> newHashtags) {
		List<PostHashtagEntity> postHashtags = findPostHashtagByPostId(post.getId());

		// 기존 post 의 hashtags 추출
		Set<String> currentHashtags = postHashtags.stream().map(
			ph -> ph.getHashtag().getContent()
		).collect(Collectors.toSet());

		Set<String> deletedHashtagContents = SetUtils.difference(currentHashtags, newHashtags);
		Set<String> updatedHashtags = SetUtils.difference(newHashtags, currentHashtags);

		// 없어진 해시태그들이 있으면 연결관계를 삭제
		if (!deletedHashtagContents.isEmpty()) {
			postHashtagRepository.deleteByPostIdAndHashtagContent(post.getId(), deletedHashtagContents);
		}

		// 새로운 해시태그들이 존재하면 저장
		if (!updatedHashtags.isEmpty()) {
			create(post, updatedHashtags);
		}
	}

}
