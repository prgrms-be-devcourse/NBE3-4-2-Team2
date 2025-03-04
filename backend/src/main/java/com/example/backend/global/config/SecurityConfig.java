package com.example.backend.global.config;

import static com.example.backend.global.config.SpringDocConfig.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.backend.identity.security.config.CustomUsernamePasswordAuthenticationFilter;
import com.example.backend.identity.security.config.handler.CustomAccessDeniedHandler;
import com.example.backend.identity.security.config.handler.CustomAuthenticationEntryPoint;
import com.example.backend.identity.security.config.handler.CustomAuthenticationFailureHandler;
import com.example.backend.identity.security.config.handler.CustomAuthenticationSuccessHandler;
import com.example.backend.identity.security.config.handler.CustomLogoutSuccessHandler;
import com.example.backend.identity.security.jwt.JwtAuthenticationFilter;
import com.example.backend.identity.security.oauth.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;
	private final CustomAccessDeniedHandler accessDeniedHandler;
	private final CustomOAuth2UserService oAuth2UserService;
	private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
	private final CustomAuthenticationFailureHandler authenticationFailureHandler;
	private final CustomLogoutSuccessHandler logoutSuccessHandler;


	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
		CustomUsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter =
			new CustomUsernamePasswordAuthenticationFilter(authenticationManager, authenticationSuccessHandler, authenticationFailureHandler);

		http
			// ✅ H2 CONSOLE 허용
			.headers(headers -> headers
				.frameOptions(frameOptions -> frameOptions.disable()) // X-Frame-Options 비활성화
			)
			// ✅ 보안 관련 설정 (CSRF, CORS, 세션)
			.csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화 (JWT 사용 시 불필요)
			.cors(cors -> corsConfigurationSource()) // CORS 설정 적용
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함 (JWT 방식)

			// ✅ 필터 설정 (JWT 인증 필터 → UsernamePasswordAuthenticationFilter)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterAt(usernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

			// ✅ OAuth2 로그인 설정
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
					.userService(oAuth2UserService)) // 사용자 정보 서비스 설정
				.successHandler(authenticationSuccessHandler)) // OAuth2 로그인 성공 핸들러

			// ✅ 로그아웃 설정
			.logout(logout -> logout
				.logoutRequestMatcher(new AntPathRequestMatcher("/api-v1/members/logout", "DELETE")) // DELETE 요청 허용
				.logoutSuccessHandler(logoutSuccessHandler)) // 로그아웃 성공 핸들러

			// ✅ 인증 및 접근 권한 설정
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/h2-console/**").permitAll() // H2 콘솔 허용
				.requestMatchers("/api-v1/members/login", "/api-v1/members/join").permitAll() // 로그인 & 회원가입 허용
				.requestMatchers(SWAGGER_PATHS).permitAll() // Swagger 문서 접근 허용
				.anyRequest().authenticated()) // 그 외 요청은 인증 필요

			// ✅ 기본 인증 방식 비활성화 (JWT 사용)
			.httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
			.formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화

			// ✅ 예외 처리 설정
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(authenticationEntryPoint) // 인증 실패 핸들러
				.accessDeniedHandler(accessDeniedHandler)); // 접근 거부 핸들러

		return http.build();
	}

	@Bean
	public UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// 허용할 오리진 설정
		configuration.setAllowedOrigins(
			Arrays.asList(AppConfig.getSiteFrontUrl(), "http://localhost:3000")); // 프론트 엔드 포트번호
		// 허용할 HTTP 메서드 설정
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // 프론트 엔드 허용 메서드
		// 자격 증명 허용 설정
		configuration.setAllowCredentials(true);
		// 허용할 헤더 설정
		configuration.setAllowedHeaders(Collections.singletonList("*"));

		configuration.setExposedHeaders(Collections.singletonList("Authorization")); // client가 Authorization 헤더를 읽을 수 있도록 해야한다.


		// CORS 설정을 소스에 등록
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() { // security를 적용하지 않을 리소스
		return web -> web.ignoring()
			// OAuth를 사용하려면 error endpoint를 열어줘야 함
			.requestMatchers("/error", "/favicon.ico");
	}

	@Bean
	public AuthenticationManager authenticationManager(
		AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}
