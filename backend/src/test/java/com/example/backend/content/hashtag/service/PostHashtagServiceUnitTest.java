package com.example.backend.content.hashtag.service;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.entity.HashtagEntity;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostHashtagEntity;
import com.example.backend.entity.PostHashtagRepository;

@ExtendWith(MockitoExtension.class)
class PostHashtagServiceUnitTest {
	@Mock
	private HashtagService hashtagService;

	@Mock
	private PostHashtagRepository postHashtagRepository;

	@InjectMocks
	private PostHashtagService postHashtagService;

	@Test
	@DisplayName("없어진 해시태그들이 없을때 아무 수행도 안해야 한다")
	void not_execute_deleteByPostIdAndHashtagContent() {
		// given
		PostEntity post = mock(PostEntity.class);
		when(post.getId()).thenReturn(1L);

		Set<String> newHashtags = Set.of("hashtag1", "hashtag2");
		List<PostHashtagEntity> currentPostHashtags = Arrays.asList(
			createPostHashtagEntity("hashtag1"),
			createPostHashtagEntity("hashtag2")
		);

		when(postHashtagRepository.findPostHashtagByPostId(1L))
			.thenReturn(currentPostHashtags);

		// when
		postHashtagService.updatePostHashtags(post, newHashtags);

		// then
		verify(postHashtagRepository, never())
			.deleteByPostIdAndHashtagContent(anyLong(), anySet());
		verify(postHashtagRepository, never()).saveAll(anyList());
	}

	@Test
	@DisplayName("새로운 해시태그들이 없으면 아무 수행도 안해야 된다")
	void not_execute_create() {
		// given
		PostEntity post = mock(PostEntity.class);
		when(post.getId()).thenReturn(1L);

		Set<String> newHashtags = Set.of("hashtag1");
		List<PostHashtagEntity> currentPostHashtags = Arrays.asList(
			createPostHashtagEntity("hashtag1")
		);

		when(postHashtagRepository.findPostHashtagByPostId(1L))
			.thenReturn(currentPostHashtags);

		// when
		postHashtagService.updatePostHashtags(post, newHashtags);

		// then
		verify(hashtagService, never()).createIfNotExists(anyString());
		verify(postHashtagRepository, never()).saveAll(anyList());
	}

	private PostHashtagEntity createPostHashtagEntity(String content) {
		HashtagEntity hashtag = mock(HashtagEntity.class);
		when(hashtag.getContent()).thenReturn(content);

		PostHashtagEntity postHashtag = mock(PostHashtagEntity.class);
		when(postHashtag.getHashtag()).thenReturn(hashtag);

		return postHashtag;
	}
}
