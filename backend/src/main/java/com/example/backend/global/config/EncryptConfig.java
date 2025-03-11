package com.example.backend.global.config;

import org.springframework.context.annotation.Configuration;


@Configuration
@Deprecated
public class EncryptConfig {
	// @Bean
	// public PasswordEncoder passwordEncoder() { // SecurityConfig에 정의하면 순환참조 -> Security 로직 변환으로 인해 순환참조 오류 발생 x
	// 	return new BCryptPasswordEncoder();
	// }
}
