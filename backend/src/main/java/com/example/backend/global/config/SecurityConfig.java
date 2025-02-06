package com.example.backend.global.config;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.backend.global.rs.ErrorRs;
import com.example.backend.global.rs.RsData;
import com.example.backend.global.util.Ut;
import com.example.backend.identity.security.filter.JwtAuthenticationFilter;

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
	// private final CumstomOAuth2UserService oAuth2UserService;
	// private final BearToke

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

			.csrf(AbstractHttpConfigurer::disable)		// CSRF 인증 방식 disable
			.cors(cors->corsConfigurationSource())
			// .oauth2Login(oauth ->
			// 	oauth.userInfoEndpoint(c -> c.userService(oAuth2UserService))
			// 		.successHandler(oAuth2SuccessHandler)
			// )
			.authorizeHttpRequests((authorizeHttpRequests) ->
				authorizeHttpRequests
					.requestMatchers("/h2-console/**")
					.permitAll()
					.requestMatchers("/api-v1/members/login", "/api-v1/members/logout", "/api-v1/members/join")
					.permitAll()
					.anyRequest()
					.authenticated()
			)
			.httpBasic(AbstractHttpConfigurer::disable) // Http Basic 인증 방식 disable
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // jwt 방식이므로 세션 x
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(
				exceptionHandling -> exceptionHandling
					.authenticationEntryPoint(
						(request, response, authException) -> {
							response.setStatus(HttpStatus.UNAUTHORIZED.value());
							response.setContentType("application/json;charset=UTF-8");

							String str = Ut.json.toString(
								RsData.error(
									ErrorRs.builder()
										.target(request.getRequestURI())
										.code(401)
										.message("사용자 인증정보가 올바르지 않습니다.")
										.build()
								)
							);

							PrintWriter writer = response.getWriter();
							writer.write(str);
							writer.flush();
							writer.close();
						}
					)
					.accessDeniedHandler(
						(request, response, accessDeniedException) -> {
							response.setStatus(HttpStatus.FORBIDDEN.value());
							response.setContentType("application/json;charset=UTF-8");

							String str = Ut.json.toString(
								RsData.error(
									ErrorRs.builder()
										.target(request.getRequestURI())
										.code(403)
										.message("해당 리소스에 권한이 없습니다.")
										.build()
								)
							);

							PrintWriter writer = response.getWriter();
							writer.write(str);
							writer.flush();
							writer.close();
						}
					)
			);
		return http.build();
	}

	@Bean
	public UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// 허용할 오리진 설정
		configuration.setAllowedOrigins(
			Arrays.asList(AppConfig.getSiteFrontUrl(), "http://localhost:3000")); // 프론트 엔드 포트번호
		// 허용할 HTTP 메서드 설정
		configuration.setAllowedMethods(Collections.singletonList("*"));//Arrays.asList("GET", "POST", "PUT", "DELETE"));
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
			// error endpoint를 열어줘야 함, favicon.ico 추가!
			.requestMatchers("/error", "/favicon.ico");
	}
}
