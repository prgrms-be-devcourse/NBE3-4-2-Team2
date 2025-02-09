package com.example.backend.social.follow.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.example.backend.entity.FollowRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.user.SecurityUser;
import com.example.backend.social.follow.dto.DeleteFollowRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FollowControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private FollowRepository followRepository;

	private String senderToken;
	private String receiverToken;
	private MemberEntity sender;
	private MemberEntity receiver;

	@BeforeEach
	public void setup() {
		// 테스트 전 데이터 초기화
		followRepository.deleteAll();
		memberRepository.deleteAll();

		// 시퀀스 초기화
		entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE follow ALTER COLUMN id RESTART WITH 1").executeUpdate();

		// 테스트용 Sender 멤버 추가
		sender = memberService.join("testSender", "testPassword", "sender@gmail.com");
		senderToken = memberService.genAccessToken(sender);

		receiver = memberService.join("testReceiver", "testPassword", "receiver@gmail.com");
		receiverToken = memberService.genAccessToken(receiver);

		// SecurityContext 설정
		SecurityUser securityUser = new SecurityUser(sender.getId(), sender.getUsername(), sender.getPassword(), new ArrayList<>());
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	@DisplayName("1. 팔로우 요청 테스트")
	public void t001() throws Exception {
		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/follow/{receiverId}", receiver.getId())
			.header("Authorization", "Bearer " + senderToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("팔로우 등록 요청에 성공했습니다."));
	}

	@Test
	@DisplayName("2. 팔로우 요청 후 취소 테스트")
	public void t002() throws Exception {
		// When & Then First
		MvcResult followResult = mockMvc.perform(post("/api-v1/follow/{receiverId}", receiver.getId())
			.header("Authorization", "Bearer " + senderToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("팔로우 등록 요청에 성공했습니다."))
			.andExpect(jsonPath("$.data").exists())
			.andReturn();

		// Given Second
		String followResponse = followResult.getResponse().getContentAsString();
		JsonNode followRoot = objectMapper.readTree(followResponse);
		Long followId = followRoot.path("data").path("followId").asLong();

		DeleteFollowRequest deleteRequest = DeleteFollowRequest.builder()
			.followId(followId)
			.build();
		String deleteRequestJson = objectMapper.writeValueAsString(deleteRequest);

		// When Second
		ResultActions resultActions = mockMvc.perform(delete("/api-v1/follow/{receiverId}", receiver.getId())
			.content(deleteRequestJson)
			.header("Authorization", "Bearer " + senderToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then Second
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("팔로우 취소 요청에 성공했습니다."));
	}

	@Test
	@DisplayName("3. 요청한 sender의 ID와 인증 정보가 다를 때 팔로우 요청 테스트")
	public void t003() throws Exception {
		// Given
		Long anotherMemberId = 99L;
		SecurityUser securityUser = new SecurityUser(anotherMemberId, "nonExistentUser", "password", new ArrayList<>());
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/follow/{receiverId}", receiver.getId())
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("멤버 정보를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("4. 존재하지 않는 receiver에게 팔로우 요청 테스트")
	public void t004() throws Exception {
		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/follow/{receiverId}", 99L)
			.header("Authorization", "Bearer " + senderToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("사용자 정보를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("5. 이미 팔로우 되있는 상대방에게 중복 팔로우 테스트")
	public void t005() throws Exception {
		// When First
		ResultActions firstResultActions = mockMvc.perform(post("/api-v1/follow/{receiverId}", receiver.getId())
			.header("Authorization", "Bearer " + senderToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then First
		firstResultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("팔로우 등록 요청에 성공했습니다."));

		// When Second
		ResultActions secondResultActions = mockMvc.perform(post("/api-v1/follow/{receiverId}", receiver.getId())
			.header("Authorization", "Bearer " + senderToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		secondResultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("이미 팔로우 상태입니다."));
	}
}
