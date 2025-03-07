package com.example.backend.social.feed.controller;

import static com.example.backend.social.feed.constant.FeedConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.global.event.CommentEventListener;
import com.example.backend.global.event.FollowEventListener;
import com.example.backend.global.event.LikeEventListener;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.jwt.AccessTokenService;
import com.example.backend.identity.security.user.CustomUser;
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

	private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

	@MockitoBean
	LikeEventListener likeEventListener;

	@MockitoBean
	FollowEventListener followEventListener;

	@MockitoBean
	CommentEventListener commentEventListener;

	@BeforeEach
	void setUp() {
		feedTestHelper.setData();

		// 멤버 로그인
		testMember = memberService.findById(1L).get();
		Assertions.assertNotNull(testMember);

		accessToken = accessTokenService.genAccessToken(testMember);

		CustomUser securityUser = new CustomUser(
			testMember,
			null
			// testMember.getId(),
			// testMember.getUsername(),
			// testMember.getPassword(),
			// new ArrayList<>()
		);

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			securityUser,
			null,
			securityUser.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	@DisplayName("피드요청 - 성공")
	void t1() throws Exception {
		LocalDateTime now = LocalDateTime.now();

		mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.param("maxSize", String.valueOf(REQUEST_FEED_MAX_SIZE))
				.param("lastPostId", "0")
				.param("timestamp", now.format(formatter))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("피드를 성공적으로 반환했습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	@DisplayName("피드요청 - 실패: 잘못된 Request 전달")
	void t2() throws Exception {
		LocalDateTime now = LocalDateTime.now();

		// null timestamp 테스트 (RequestParam 에서는 파라미터를 생략하여 테스트)
		mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.param("maxSize", String.valueOf(REQUEST_FEED_MAX_SIZE))
				.param("lastPostId", "0")
				.param("timestamp", "")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		// 미래 타임스탬프 테스트
		mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.param("maxSize", String.valueOf(REQUEST_FEED_MAX_SIZE))
				.param("lastPostId", "0")
				.param("timestamp", now.plusDays(1).format(formatter))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		// 최대 크기 초과 테스트
		mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.param("maxSize", String.valueOf(REQUEST_FEED_MAX_SIZE + 1))
				.param("lastPostId", "0")
				.param("timestamp", now.minusDays(1).format(formatter))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("피드요청 - 성공: 빈 리스트")
	void t3() throws Exception {
		LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

		ResultActions resultActions = mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.param("maxSize", String.valueOf(REQUEST_FEED_MAX_SIZE))
				.param("lastPostId", "0")
				.param("timestamp", yesterday.format(formatter))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("피드를 성공적으로 반환했습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	@DisplayName("피드요청 - 성공: 재요청까지 성공")
	void t4() throws Exception {
		LocalDateTime now = LocalDateTime.now();

		ResultActions resultActions = mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.param("maxSize", "2")
				.param("lastPostId", "0")
				.param("timestamp", now.format(formatter))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
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

		resultActions = mockMvc.perform(get("/api-v1/feed")
				.header("Authorization", "Bearer " + accessToken)
				.param("maxSize", "2")
				.param("lastPostId", lastPostId1.toString())
				.param("timestamp", lastTime1.format(formatter))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
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
