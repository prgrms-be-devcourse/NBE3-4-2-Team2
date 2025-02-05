package com.example.backend.identity;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.global.rs.ErrorRs;
import com.example.backend.global.rs.RsData;
import com.example.backend.identity.member.controller.ApiV1MemberController;
import com.example.backend.identity.member.dto.login.MemberLoginRequest;
import com.example.backend.identity.member.service.MemberService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1MemberControllerTest {
	@Autowired
	private MemberService memberService;
	@Autowired
	private MockMvc mvc;
	@Autowired
	private MemberRepository memberRepository;

	@Test
	@DisplayName("회원가입 성공")
	void t1() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				post("/api-v1/members/join")
					.content("""
						{
						    "username" : "newUser",
						    "password" : "@q1w2e3r4@",
						    "email" : "newUser@naver.com"
						}
						""".stripIndent())
					.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
					)
			)
			.andDo(print());

		MemberEntity member = memberService.findByUsername("newUser").get();

		resultActions
			.andExpect(handler().handlerType(ApiV1MemberController.class))
			.andExpect(handler().methodName("join"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.message").value("%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getUsername())))
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.id").value(member.getId()))
			.andExpect(jsonPath("$.data.username").value(member.getUsername()));
	}

	@Test
	@DisplayName("회원가입 실패")
	void t2() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				post("/api-v1/members/join")
					.content("""
						{
						    "username": "",
						    "password": "",
						    "nickname": "test"
						}
						""".stripIndent())
					.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
					)
			)
			.andDo(print());
		List<ErrorRs> errors = List.of(new ErrorRs[] {
			new ErrorRs("email", 400, "공백일 수 없습니다."),
			new ErrorRs("password", 400, "공백일 수 없습니다."),
			new ErrorRs("password", 400, "비밀번호는 10자 이상이며, 숫자와 특수문자를 포함해야 합니다.")
		});

		resultActions
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("join"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("유효성 검증에 실패하였습니다."))
				.andExpect(jsonPath("$.data").exists())
				.andExpect(jsonPath("$.data[0].target").value("email"))
				.andExpect(jsonPath("$.data[0].message").value("must not be blank")) // locale 설정 안해서 테스트에선 디폴트(영어)
				.andExpect(jsonPath("$.data[1].target").value("password"))
				.andExpect(jsonPath("$.data[1].message").value("must not be blank"))
				.andExpect(jsonPath("$.data[2].target").value("password"))
				.andExpect(jsonPath("$.data[2].message").value("비밀번호는 10자 이상이며, 숫자와 특수문자를 포함해야 합니다."));
	}

	@Test
	@DisplayName("회원가입 시 이미 사용중인 username")
	void t3() throws Exception {
		memberService.join("user1","@q1w2e3r4@", "user1@naver.com");

		memberRepository.flush();

		ResultActions resultActions = mvc
			.perform(
				post("/api-v1/members/join")
					.content("""
						{
						    "username": "user1",
						    "password": "@q1w2e3r4@",
						    "email": "user12@naver.com"
						}
						""".stripIndent())
					.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
					)
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(ApiV1MemberController.class))
			.andExpect(handler().methodName("join"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.data.code").value("409"))
			.andExpect(jsonPath("$.data.message").value("해당 username은 이미 사용중입니다."))
			.andExpect(jsonPath("$.success").value(false));
	}



	@Test
	@DisplayName("로그인 성공")
	void t4() throws Exception {
		memberService.join("user1","@q1w2e3r4@", "user1@naver.com");

		memberRepository.flush();

		ResultActions resultActions = mvc
			.perform(
				post("/api-v1/members/login")
					.content("""
						{
						    "username": "user1",
						    "password": "@q1w2e3r4@"
						}
						""".stripIndent())
					.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
					)
			)
			.andDo(print());

		MemberEntity member = memberService.findByUsername("user1").get();

		resultActions
			.andExpect(handler().handlerType(ApiV1MemberController.class))
			.andExpect(handler().methodName("login"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("%s님 환영합니다.".formatted(member.getUsername())))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.username").value("user1"))
			.andExpect(jsonPath("$.data.profileUrl").isEmpty());


		resultActions.andExpect(
			result -> {
				Cookie accessTokenCookie = result.getResponse().getCookie("access_token");
				assertThat(accessTokenCookie.getValue()).isNotBlank();
				assertThat(accessTokenCookie.getPath()).isEqualTo("/");
				assertThat(accessTokenCookie.isHttpOnly()).isTrue();
				assertThat(accessTokenCookie.getSecure()).isTrue();

				Cookie refreshTokenCookie = result.getResponse().getCookie("refresh_token");
				assertThat(refreshTokenCookie.getValue()).isEqualTo(member.getRefreshToken());
				assertThat(refreshTokenCookie.getPath()).isEqualTo("/");
				assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
				assertThat(refreshTokenCookie.getSecure()).isTrue();
			});
	}

	@Test
	@DisplayName("로그인, wrong username")
	void t5() throws Exception {
		memberService.join("user1","@q1w2e3r4@", "user1@naver.com");

		memberRepository.flush();

		ResultActions resultActions = mvc
			.perform(
				post("/api-v1/members/login")
					.content("""
						{
						    "username": "user",
						    "password": "@q1w2e3r4@"
						}
						""".stripIndent())
					.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
					)
			)
			.andDo(print());


		resultActions
			.andExpect(handler().handlerType(ApiV1MemberController.class))
			.andExpect(handler().methodName("login"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.message").value("사용자 정보가 존재하지 않습니다."));
	}

	@Test
	@DisplayName("로그인, without password")
	void t6() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				post("/api-v1/members/login")
					.content("""
						{
						    "username": "user1",
						    "password": ""
						}
						""".stripIndent())
					.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
					)
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(ApiV1MemberController.class))
			.andExpect(handler().methodName("login"))
			.andExpect(status().isBadRequest())
			// .andExpect(jsonPath("$.data.code").value(400))
			.andExpect(jsonPath("$.data[0].message").value("비밀번호를 입력해주세요."))
			.andExpect(jsonPath("$.success").value(false));
	}
}
