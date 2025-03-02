package com.example.backend.identity;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.global.rs.ErrorRs;
import com.example.backend.identity.member.controller.MemberController;
import com.example.backend.identity.member.service.MemberService;

import jakarta.servlet.http.Cookie;

@SpringBootTest
// @ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {
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
			.andExpect(handler().handlerType(MemberController.class))
			.andExpect(handler().methodName("join"))
			.andExpect(status().isOk())
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
				.andExpect(handler().handlerType(MemberController.class))
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
			.andExpect(handler().handlerType(MemberController.class))
			.andExpect(handler().methodName("join"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("해당 사용자가 이미 존재합니다."))
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
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("%s님 환영합니다.".formatted(member.getUsername())))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.username").value("user1"));
			// .andExpect(jsonPath("$.data.profileUrl").isEmpty());


		resultActions.andExpect(
			result -> {
				String accessToken = result.getResponse().getHeader("Authorization");
				assertThat(accessToken).isNotBlank();

				Cookie refreshTokenCookie = result.getResponse().getCookie("refresh_token");
				assertThat(refreshTokenCookie.getPath()).isEqualTo("/");
				assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
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
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("인증 정보가 일치하지 않습니다."));
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
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value("인증 정보가 일치하지 않습니다."))
			.andExpect(jsonPath("$.success").value(false));
	}
}
