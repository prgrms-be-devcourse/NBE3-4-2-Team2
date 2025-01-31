package com.example.backend.content.hashtag.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class HashtagExtractor {

	private static final Pattern HASHTAG_EXTRACT_PATTERN = Pattern.compile("#([\\w가-힣]+)");

	public Set<String> extractHashtag(String content) {
		Set<String> hashtags = new LinkedHashSet<>();
		Matcher matcher = HASHTAG_EXTRACT_PATTERN.matcher(content);

		while (matcher.find()) {
			hashtags.add(matcher.group(1));
		}

		return hashtags;
	}
}
