package com.example.backend.global.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 *
 * 스프링 시큐리티 설정
 * H2-console 페이지 허용
 *
 * @author Metronon
 * @since 25. 1. 28.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/**
	 *
	 * 접속 url 관리
	 *
	 * @param http
	 * @return
	 * @throws Exception
	 */

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((authorizeHttpRequests) ->
				authorizeHttpRequests.requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
					.requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll())
			.csrf((csrf) -> csrf.disable())
			.headers((headers) ->
				headers.addHeaderWriter(
					new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)));
		return http.build();
	}

	@Bean
	public UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// 허용할 오리진 설정
		configuration.setAllowedOrigins(
			Arrays.asList(AppConfig.getSiteFrontUrl(), "http://localhost:8080"));
		// 허용할 HTTP 메서드 설정
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
		// 자격 증명 허용 설정
		configuration.setAllowCredentials(true);
		// 허용할 헤더 설정
		configuration.setAllowedHeaders(List.of("*"));
		// CORS 설정을 소스에 등록
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
