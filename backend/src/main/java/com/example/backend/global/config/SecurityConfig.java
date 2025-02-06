// package com.example.backend.global.config;
//
// import java.util.Arrays;
// import java.util.List;
//
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
// import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
// import com.example.backend.global.rs.RsData;
// import com.example.backend.global.util.Ut;
// import com.example.backend.identity.security.filter.JwtAuthenticationFilter;
//
// import lombok.RequiredArgsConstructor;
//
// /**
//  *
//  * 스프링 시큐리티 설정
//  * H2-console 페이지 허용
//  *
//  * @author Metronon
//  * @since 25. 1. 28.
//  */
// @Configuration
// @EnableWebSecurity
// @RequiredArgsConstructor
// public class SecurityConfig {
// 	private final JwtAuthenticationFilter jwtAuthenticationFilter;
//
// 	/**
// 	 *
// 	 * 접속 url 관리
// 	 *
// 	 * @param http
// 	 * @return
// 	 * @throws Exception
// 	 */
//
// 	@Bean
// 	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
// 		http
// 			.authorizeHttpRequests((authorizeHttpRequests) ->
// 				authorizeHttpRequests
// 					.requestMatchers("/h2-console/**")
// 					.permitAll()
// 					.requestMatchers("/api-v1/members/login", "/api-v1/members/logout", "/api-v1/members/join")
// 					.permitAll()
// 					.anyRequest()
// 					.authenticated()
// 			)
// 			.csrf((csrf) -> csrf.disable())
// 			.headers((headers) ->
// 				headers.addHeaderWriter(
// 					new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
// 			.headers(
// 				headers ->
// 					headers.frameOptions(
// 						frameOptions ->
// 							frameOptions.sameOrigin()
// 					)
// 			)
// 			.csrf(
// 				csrf ->
// 					csrf.disable()
// 			)
// 			.cors(
// 				cors->corsConfigurationSource()
// 			)
// 			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
// 			.exceptionHandling(
// 				exceptionHandling -> exceptionHandling
// 					.authenticationEntryPoint(
// 						(request, response, authException) -> {
// 							response.setContentType("application/json;charset=UTF-8");
//
// 							response.setStatus(401);
// 							response.getWriter().write(
// 								Ut.json.toString(
// 									// new RsData("401-1", "사용자 인증정보가 올바르지 않습니다.")
// 									RsData.error(
// 										"사용자 인증정보가 올바르지 않습니다.")
// 								)
// 							);
// 						}
// 					)
// 					.accessDeniedHandler(
// 						(request, response, accessDeniedException) -> {
// 							response.setContentType("application/json;charset=UTF-8");
//
// 							response.setStatus(403);
// 							response.getWriter().write(
// 								Ut.json.toString(
// 									// new RsData("403-1", "권한이 없습니다.")
// 									RsData.error(null, "권한이 없습니다.")
// 								)
// 							);
// 						}
// 					)
// 			);
//
// 		return http.build();
// 	}
//
// 	@Bean
// 	public UrlBasedCorsConfigurationSource corsConfigurationSource() {
// 		CorsConfiguration configuration = new CorsConfiguration();
// 		// 허용할 오리진 설정
// 		configuration.setAllowedOrigins(
// 			Arrays.asList(AppConfig.getSiteFrontUrl(), "http://localhost:8080"));
// 		// 허용할 HTTP 메서드 설정
// 		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
// 		// 자격 증명 허용 설정
// 		configuration.setAllowCredentials(true);
// 		// 허용할 헤더 설정
// 		configuration.setAllowedHeaders(List.of("*"));
// 		// CORS 설정을 소스에 등록
// 		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
// 		source.registerCorsConfiguration("/**", configuration);
// 		return source;
// 	}
// }
