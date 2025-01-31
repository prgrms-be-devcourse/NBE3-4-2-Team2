package com.example.backend.global.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

/**
 * 어플리케이션 설정
 *
 * @author Metronon
 * @since 25. 1. 28.
 */
@Configuration
public class AppConfig {
	@Getter
	private static ObjectMapper objectMapper;

	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		AppConfig.objectMapper = objectMapper;
	}


	// 프론트엔드 주소 미리 추가
	public static String getSiteFrontUrl() {
		return "http://localhost:3000";
	}
}
