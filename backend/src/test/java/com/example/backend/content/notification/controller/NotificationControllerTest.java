package com.example.backend.content.notification.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.backend.content.notification.type.NotificationType;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.NotificationEntity;
import com.example.backend.entity.NotificationRepository;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.jwt.AccessTokenService;
import com.example.backend.identity.security.user.CustomUser;

import jakarta.transaction.Transactional;

/**
 * @author kwak
 * 2025. 2. 19.
 */

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	NotificationRepository notificationRepository;
	@Autowired
	MemberService memberService;

	@Autowired
	AccessTokenService accessTokenService;

	@Test
	@DisplayName("알림 목록 정상적으로 가져오기")
	void test1() throws Exception {
		Long targetId = 1L;

		NotificationEntity test1 = NotificationEntity.create("test1", 1L, NotificationType.LIKE, targetId);
		NotificationEntity test2 = NotificationEntity.create("test2", 1L, NotificationType.LIKE, targetId);
		NotificationEntity test3 = NotificationEntity.create("test3", 1L, NotificationType.FOLLOW, targetId);
		notificationRepository.save(test1);
		notificationRepository.save(test2);
		notificationRepository.save(test3);

		MemberEntity member = memberService.join("user1", "password1234", "user1@test.com");
		CustomUser customUser = new CustomUser(member, null);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			customUser, null, customUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		ResultActions resultActions = mockMvc.perform(get("/api-v1/notification/list")
			.param("page", "0")
			.with(user(customUser))
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		resultActions
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.data.responses", hasSize(3)))
			.andExpect(jsonPath("$.data.totalCount").value(3))
			.andExpect(jsonPath("$.data.currentPage").value(0))
			.andExpect(jsonPath("$.data.totalPageCount").value(1));
	}
}
