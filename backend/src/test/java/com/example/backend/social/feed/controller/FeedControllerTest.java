package com.example.backend.social.feed.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.social.feed.implement.FeedTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@BeforeEach
	void setUp() {
		feedTestHelper.setData();
	}

	// @Test
	// @DisplayName("피드요청 - 성공")
	// void t1() throws Exception {
	// 	FeedRequest request = FeedRequest.builder()
	// 		.maxSize(REQUEST_FEED_MAX_SIZE)
	// 		.lastPostId(null)
	// 		.timestamp(LocalDateTime.now())
	// 		.username("user1")
	// 		.build();
	//
	// 	mockMvc.perform(get("/api-v1/feed")
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.accept(MediaType.APPLICATION_JSON)
	// 			.content(objectMapper.writeValueAsString(request)))
	// 		.andExpect(status().isOk())
	// 		.andExpect(jsonPath("$.success").value(true))
	// 		.andExpect(jsonPath("$.message").value("피드를 성공적으로 반환했습니다."))
	// 		.andExpect(jsonPath("$.data").exists());
	// }
	//
	// @Test
	// @DisplayName("피드요청 - 실패: 잘못된 Request 전달")
	// void t2() throws Exception {
	// 	FeedRequest nullTimestamp = FeedRequest.builder()
	// 		.maxSize(REQUEST_FEED_MAX_SIZE)
	// 		.lastPostId(null)
	// 		.timestamp(null)
	// 		.username("user1")
	// 		.build();
	//
	// 	FeedRequest afterTimestamp = FeedRequest.builder()
	// 		.maxSize(REQUEST_FEED_MAX_SIZE)
	// 		.lastPostId(null)
	// 		.timestamp(LocalDateTime.now().plusDays(1))
	// 		.username("user1")
	// 		.build();
	//
	// 	FeedRequest overMaxSize = FeedRequest.builder()
	// 		.maxSize(REQUEST_FEED_MAX_SIZE + 1)
	// 		.lastPostId(null)
	// 		.timestamp(LocalDateTime.now().minusDays(1))
	// 		.username("user1")
	// 		.build();
	//
	// 	mockMvc.perform(get("/api-v1/feed")
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.accept(MediaType.APPLICATION_JSON)
	// 			.content(objectMapper.writeValueAsString(nullTimestamp)))
	// 		.andExpect(status().isBadRequest())
	// 		.andExpect(jsonPath("$.success").value(false))
	// 		.andExpect(jsonPath("$.message").value("유효하지 않은 타임스탬프입니다."))
	// 		.andExpect(jsonPath("$.data").exists());
	//
	// 	mockMvc.perform(get("/api-v1/feed")
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.accept(MediaType.APPLICATION_JSON)
	// 			.content(objectMapper.writeValueAsString(afterTimestamp)))
	// 		.andExpect(status().isBadRequest())
	// 		.andExpect(jsonPath("$.success").value(false))
	// 		.andExpect(jsonPath("$.message").value("유효하지 않은 타임스탬프입니다."))
	// 		.andExpect(jsonPath("$.data").exists());
	//
	// 	mockMvc.perform(get("/api-v1/feed")
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.accept(MediaType.APPLICATION_JSON)
	// 			.content(objectMapper.writeValueAsString(overMaxSize)))
	// 		.andExpect(status().isBadRequest())
	// 		.andExpect(jsonPath("$.success").value(false))
	// 		.andExpect(jsonPath("$.message").value("유효하지 않은 범위의 요청 개수입니다."))
	// 		.andExpect(jsonPath("$.data").exists());
	// }
	//
	// @Test
	// @DisplayName("피드요청 - 성공: 빈 리스트")
	// void t3() throws Exception {
	// 	FeedRequest notEnoughFeedRequest = FeedRequest.builder()
	// 		.maxSize(REQUEST_FEED_MAX_SIZE)
	// 		.lastPostId(null)
	// 		.timestamp(LocalDateTime.now().minusDays(1))
	// 		.username("user1")
	// 		.build();
	//
	// 	ResultActions resultActions = mockMvc.perform(get("/api-v1/feed")
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.accept(MediaType.APPLICATION_JSON)
	// 			.content(objectMapper.writeValueAsString(notEnoughFeedRequest)))
	// 		.andExpect(status().isOk())
	// 		.andExpect(jsonPath("$.success").value(true))
	// 		.andExpect(jsonPath("$.message").value("피드를 성공적으로 반환했습니다."))
	// 		.andExpect(jsonPath("$.data").exists())
	// 		.andExpect(jsonPath("$.data.feedList.length()").value(0));
	// }
	//
	// @Test
	// @DisplayName("피드요청 - 성공: 재요청까지 성공")
	// void t4() throws Exception {
	// 	FeedRequest firstRequest = FeedRequest.builder()
	// 		.maxSize(2)
	// 		.lastPostId(null)
	// 		.timestamp(LocalDateTime.now())
	// 		.username("user1")
	// 		.build();
	//
	// 	ResultActions resultActions = mockMvc.perform(get("/api-v1/feed")
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.accept(MediaType.APPLICATION_JSON)
	// 			.content(objectMapper.writeValueAsString(firstRequest)))
	// 		.andExpect(status().isOk())
	// 		.andExpect(jsonPath("$.success").value(true))
	// 		.andExpect(jsonPath("$.message").value("피드를 성공적으로 반환했습니다."))
	// 		.andExpect(jsonPath("$.data").exists());
	//
	// 	String responseContent = resultActions.andReturn()
	// 		.getResponse()
	// 		.getContentAsString();
	//
	// 	List<Map<String, Object>> feedList1 = JsonPath.read(responseContent, "$.data.feedList");
	// 	Number lastPostId1 = JsonPath.read(responseContent, "$.data.lastPostId");
	// 	String lastTimestamp1 = JsonPath.read(responseContent, "$.data.lastTimestamp");
	// 	LocalDateTime lastTime1 = LocalDateTime.parse(lastTimestamp1);
	//
	// 	Assertions.assertEquals(2, feedList1.size());
	//
	// 	FeedRequest secondRequest = FeedRequest.builder()
	// 		.maxSize(2)
	// 		.lastPostId(lastPostId1.longValue())
	// 		.timestamp(lastTime1)
	// 		.username("user1")
	// 		.build();
	//
	// 	resultActions = mockMvc.perform(get("/api-v1/feed")
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.accept(MediaType.APPLICATION_JSON)
	// 			.content(objectMapper.writeValueAsString(secondRequest)))
	// 		.andExpect(status().isOk())
	// 		.andExpect(jsonPath("$.success").value(true))
	// 		.andExpect(jsonPath("$.message").value("피드를 성공적으로 반환했습니다."))
	// 		.andExpect(jsonPath("$.data").exists());
	//
	// 	responseContent = resultActions.andReturn()
	// 		.getResponse()
	// 		.getContentAsString();
	//
	// 	List<Map<String, Object>> feedList2 = JsonPath.read(responseContent, "$.data.feedList");
	// 	Number lastPostId2 = JsonPath.read(responseContent, "$.data.lastPostId");
	// 	String lastTimestamp2 = JsonPath.read(responseContent, "$.data.lastTimestamp");
	// 	LocalDateTime lastTime2 = LocalDateTime.parse(lastTimestamp2);
	//
	// 	// 1번째 요청과 2번째 요청 비교
	// 	Assertions.assertTrue(lastTime1.equals(lastTime2) || lastTime1.isAfter(lastTime2));
	// 	for (Map<String, Object> feed1 : feedList1) {
	// 		for (Map<String, Object> feed2 : feedList2) {
	// 			Assertions.assertNotEquals(feed1.get("postId"), feed2.get("postId"));
	// 		}
	// 	}
	// }
}
