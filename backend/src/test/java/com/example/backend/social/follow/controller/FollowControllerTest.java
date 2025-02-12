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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.example.backend.entity.FollowEntity;
import com.example.backend.entity.FollowRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.global.event.FollowEventListener;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.jwt.AccessTokenService;
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
	private AccessTokenService accessTokenService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private FollowRepository followRepository;
	@MockitoBean
	FollowEventListener followEventListener;

	private String senderToken;
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
		senderToken = accessTokenService.genAccessToken(sender);

		receiver = memberService.join("testReceiver", "testPassword", "receiver@gmail.com");

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
	@DisplayName("3. 존재하지 않는 receiver에게 팔로우 요청 테스트")
	public void t003() throws Exception {
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
	@DisplayName("4. 이미 팔로우 되있는 상대방에게 중복 팔로우 테스트")
	public void t004() throws Exception {
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
		secondResultActions.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("이미 팔로우 상태입니다."));
	}

	@Test
	@DisplayName("5. 팔로우 관계가 아닌 상대방에게 팔로우 취소 요청 테스트")
	public void t005() throws Exception {
		// Given
		DeleteFollowRequest deleteRequest = DeleteFollowRequest.builder()
			.followId(receiver.getId())
			.build();
		String deleteRequestJson = objectMapper.writeValueAsString(deleteRequest);

		// When
		ResultActions resultActions = mockMvc.perform(delete("/api-v1/follow/{receiverId}", receiver.getId())
			.content(deleteRequestJson)
			.header("Authorization", "Bearer " + senderToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("팔로우 관계를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("6. 팔로우 취소시 다른 유저가 요청하는 테스트")
	public void t006() throws Exception {
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
		Long followId = 99L;

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
		resultActions.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("팔로우 관계를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("7. DB에 등록된 팔로우 관계와 receiver가 일치하지 않는 테스트")
	public void t007() throws Exception {
		// When & Then First
		MvcResult followResult = mockMvc.perform(post("/api-v1/follow/{receiverId}", receiver.getId())
				.header("Authorization", "Bearer " + senderToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("팔로우 등록 요청에 성공했습니다."))
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
		ResultActions resultActions = mockMvc.perform(delete("/api-v1/follow/{receiverId}", 99L)
			.content(deleteRequestJson)
			.header("Authorization", "Bearer " + senderToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then Second
		resultActions.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("잘못된 팔로우 취소 요청입니다."));
	}

	@Test
	@DisplayName("8. 자기 자신을 팔로우 하는 테스트")
	public void t008() throws Exception {
		// When & Then First
		mockMvc.perform(post("/api-v1/follow/{receiverId}", sender.getId())
				.header("Authorization", "Bearer " + senderToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("자기 자신을 팔로우 할 수 없습니다."));
	}

	@Test
	@DisplayName("9. 자기 자신을 언팔로우 하는 테스트")
	public void t09() throws Exception {
		// Given
		FollowEntity follow = FollowEntity.create(sender, sender);
		followRepository.save(follow);

		DeleteFollowRequest deleteRequest = DeleteFollowRequest.builder()
			.followId(1L)
			.build();
		String deleteRequestJson = objectMapper.writeValueAsString(deleteRequest);

		// When & Then First
		mockMvc.perform(delete("/api-v1/follow/{receiverId}", sender.getId())
				.header("Authorization", "Bearer " + senderToken)
				.content(deleteRequestJson)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("자기 자신을 언팔로우 할 수 없습니다."));
	}
}
