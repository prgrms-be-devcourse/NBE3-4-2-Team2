package com.example.backend.content.hashtag.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HashtagExtractorTest {

	@Autowired
	HashtagExtractor hashtagExtractor;

	@Test
	void extractHashtag() {

		String content = "#고양이 #강아지#고양이#개구리 너무너무 귀여워";

		Set<String> result = hashtagExtractor.extractHashtag(content);

		assertThat(result.size()).isEqualTo(3);
		assertThat(result).contains("고양이");
		assertThat(result).contains("강아지");
		assertThat(result).contains("개구리");

	}
}
