package com.example.backend.social.feed.controller;

import static com.example.backend.social.feed.constant.FeedConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.jwt.AccessTokenService;
import com.example.backend.identity.security.user.SecurityUser;
import com.example.backend.social.feed.dto.FeedRequest;
import com.example.backend.social.feed.implement.FeedTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@Transactional
@DirtiesContext
@AutoConfigureMockMvc
class FeedControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private FeedTestHelper feedTestHelper;

	@Autowired
	private MemberService memberService;

	@Autowired
	private AccessTokenService accessTokenService;

	private String accessToken;
	private MemberEntity testMember;

	@BeforeEach
	void setUp() {
		feedTestHelper.setData();

		// 멤버 로그인
		testMember = memberService.findById(1L).get();
		Assertions.assertNotNull(testMember);

		accessToken = accessTokenService.genAccessToken(testMember);

		SecurityUser securityUser = new SecurityUser(
			testMember.getId(),
			testMember.getUsername(),
			testMember.getPassword(),
			new ArrayList<>());

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			securityUser,
			null,
			securityUser.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	@DisplayName("피드요청 - 성공")
	void t1() throws Exception {
		FeedRequest request = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE)
			.lastPostId(0L)
			.timestamp(LocalDateTime.now())
			.build();

		mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("피드를 성공적으로 반환했습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	@DisplayName("피드요청 - 실패: 잘못된 Request 전달")
	void t2() throws Exception {

		FeedRequest nullTimestamp = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE)
			.lastPostId(0L)
			.timestamp(null)
			.build();

		FeedRequest afterTimestamp = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE)
			.lastPostId(0L)
			.timestamp(LocalDateTime.now().plusDays(1))
			.build();

		FeedRequest overMaxSize = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE + 1)
			.lastPostId(0L)
			.timestamp(LocalDateTime.now().minusDays(1))
			.build();

		mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(nullTimestamp)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("유효하지 않은 타임스탬프입니다."));

		mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(afterTimestamp)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("유효하지 않은 타임스탬프입니다."));

		mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(overMaxSize)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("유효하지 않은 범위의 요청 개수입니다."));
	}

	@Test
	@DisplayName("피드요청 - 성공: 빈 리스트")
	void t3() throws Exception {
		FeedRequest notEnoughFeedRequest = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE)
			.lastPostId(0L)
			.timestamp(LocalDateTime.now().minusDays(1))
			.build();

		ResultActions resultActions = mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(notEnoughFeedRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("피드를 성공적으로 반환했습니다."))
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.feedList.length()").value(0));
	}

	@Test
	@DisplayName("피드요청 - 성공: 재요청까지 성공")
	void t4() throws Exception {
		FeedRequest firstRequest = FeedRequest.builder()
			.maxSize(2)
			.lastPostId(0L)
			.timestamp(LocalDateTime.now())
			.build();

		ResultActions resultActions = mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(firstRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("피드를 성공적으로 반환했습니다."))
			.andExpect(jsonPath("$.data").exists());

		String responseContent = resultActions.andReturn()
			.getResponse()
			.getContentAsString();

		List<Map<String, Object>> feedList1 = JsonPath.read(responseContent, "$.data.feedList");
		Number lastPostId1 = JsonPath.read(responseContent, "$.data.lastPostId");
		String lastTimestamp1 = JsonPath.read(responseContent, "$.data.lastTimestamp");
		LocalDateTime lastTime1 = LocalDateTime.parse(lastTimestamp1);

		Assertions.assertEquals(2, feedList1.size());

		FeedRequest secondRequest = FeedRequest.builder()
			.maxSize(2)
			.lastPostId(lastPostId1.longValue())
			.timestamp(lastTime1)
			.build();

		resultActions = mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(secondRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("피드를 성공적으로 반환했습니다."))
			.andExpect(jsonPath("$.data").exists());

		responseContent = resultActions.andReturn()
			.getResponse()
			.getContentAsString();

		List<Map<String, Object>> feedList2 = JsonPath.read(responseContent, "$.data.feedList");
		String lastTimestamp2 = JsonPath.read(responseContent, "$.data.lastTimestamp");
		LocalDateTime lastTime2 = LocalDateTime.parse(lastTimestamp2);

		// 1번째 요청과 2번째 요청 비교
		Assertions.assertTrue(lastTime1.equals(lastTime2) || lastTime1.isAfter(lastTime2));
		for (Map<String, Object> feed1 : feedList1) {
			for (Map<String, Object> feed2 : feedList2) {
				Assertions.assertNotEquals(feed1.get("postId"), feed2.get("postId"));
			}
		}
	}
}
