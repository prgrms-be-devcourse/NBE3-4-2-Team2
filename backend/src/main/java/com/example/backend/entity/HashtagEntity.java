package com.example.backend.entity;

import java.util.regex.Pattern;

import com.example.backend.content.hashtag.exception.HashtagErrorCode;
import com.example.backend.content.hashtag.exception.HashtagException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "hashtag")
public class HashtagEntity {

	private static final Pattern HASHTAG_PATTERN = Pattern.compile("^[\\w가-힣]+$");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String content;

	public HashtagEntity(String content) {
		validateContent(content);
		this.content = content;
	}

	private void validateContent(String content) {

		if (content == null || content.isBlank()) {
			throw new HashtagException(HashtagErrorCode.EMPTY_CONTENT);
		}
		if (content.length() > 10) {
			throw new HashtagException(HashtagErrorCode.TOO_LONG_HASHTAG_CONTENT);
		}
		if (!HASHTAG_PATTERN.matcher(content).matches()) {
			throw new HashtagException(HashtagErrorCode.INVALID_HASHTAG_CONTENT);
		}

	}
}
