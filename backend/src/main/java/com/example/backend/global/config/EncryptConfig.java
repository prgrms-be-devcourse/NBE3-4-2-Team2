package com.example.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class EncryptConfig {
	@Bean
	public PasswordEncoder passwordEncoder() { // SecurityConfig에 정의하면 순환참조
		return new BCryptPasswordEncoder();
	}
}
