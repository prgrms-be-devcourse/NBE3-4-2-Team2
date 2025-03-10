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
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.global.event.FollowEventListener;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.jwt.AccessTokenService;
import com.example.backend.identity.security.user.SecurityUser;
import com.example.backend.social.follow.service.FollowService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;

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
    private FollowService followService;

    @MockitoBean
    FollowEventListener followEventListener;

    private String senderToken;
    private MemberEntity sender;
    private MemberEntity receiver;

    @BeforeEach
    public void setup() {
        // 테스트 전 데이터 초기화
        memberRepository.deleteAll();

        // 시퀀스 초기화
        entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();

        // 테스트용 Sender 멤버 추가
        sender = memberService.join("testSender", "testPassword", "sender@gmail.com");
        senderToken = accessTokenService.genAccessToken(sender);

        // 테스트용 Receiver 멤버 추가
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
        mockMvc.perform(post("/api-v1/member/follow/{receiver}", receiver.getUsername())
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            // Then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("팔로우 등록에 성공했습니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.senderUsername").value(sender.getUsername()))
            .andExpect(jsonPath("$.data.receiverUsername").value(receiver.getUsername()))
            .andExpect(jsonPath("$.data.timestamp").exists());
    }

    @Test
    @DisplayName("2. 팔로우 요청 후 취소 테스트")
    public void t002() throws Exception {
        // Given: First create a follow relationship
        mockMvc.perform(post("/api-v1/member/follow/{receiver}", receiver.getUsername())
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // When: Then delete the follow relationship
        mockMvc.perform(delete("/api-v1/member/follow/{receiver}", receiver.getUsername())
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            // Then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("팔로우 취소에 성공했습니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.senderUsername").value(sender.getUsername()))
            .andExpect(jsonPath("$.data.receiverUsername").value(receiver.getUsername()))
            .andExpect(jsonPath("$.data.timestamp").exists());
    }

    @Test
    @DisplayName("3. 존재하지 않는 receiver에게 팔로우 요청 테스트")
    public void t003() throws Exception {
        // When
        mockMvc.perform(post("/api-v1/member/follow/{receiver}", "nonExistentUser")
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            // Then
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("응답측 회원 검증에 실패했습니다."));
    }

    @Test
    @DisplayName("4. 이미 팔로우 된 상대방에게 중복 팔로우 테스트")
    public void t004() throws Exception {
        // Given: First create a follow relationship
        mockMvc.perform(post("/api-v1/member/follow/{receiver}", receiver.getUsername())
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // When: Try to follow again
        mockMvc.perform(post("/api-v1/member/follow/{receiver}", receiver.getUsername())
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            // Then
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("이미 팔로우 상태입니다."));
    }

    @Test
    @DisplayName("5. 팔로우 관계가 아닌 상대방에게 팔로우 취소 요청 테스트")
    public void t005() throws Exception {
        // When
        mockMvc.perform(delete("/api-v1/member/follow/{receiver}", receiver.getUsername())
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            // Then
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("팔로우 관계를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("6. 자기 자신을 팔로우 하는 테스트")
    public void t006() throws Exception {
        // When
        mockMvc.perform(post("/api-v1/member/follow/{receiver}", sender.getUsername())
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            // Then: This requires implementing a self-follow check in your service
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("자기 자신에게 해당 작업을 수행할 수 없습니다."));
    }

    @Test
    @DisplayName("7. 자기 자신을 언팔로우 하는 테스트")
    public void t007() throws Exception {
        // When
        mockMvc.perform(delete("/api-v1/member/follow/{receiver}", sender.getUsername())
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            // Then: This requires implementing a self-unfollow check in your service
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("자기 자신에게 해당 작업을 수행할 수 없습니다."));
    }

    @Test
    @DisplayName("8. 맞팔로우 확인 테스트 - 맞팔로우 상태")
    public void t008() throws Exception {
        // Given: Create mutual follow
        followService.createFollow(sender.getUsername(), receiver.getUsername());
        followService.createFollow(receiver.getUsername(), sender.getUsername());

        // When
        mockMvc.perform(get("/api-v1/member/mutual/{receiver}", receiver.getUsername())
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            // Then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("맞팔로우 여부 조회에 성공했습니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.isMutualFollow").value(true));
    }

    @Test
    @DisplayName("9. 맞팔로우 확인 테스트 - 단방향 팔로우 상태")
    public void t009() throws Exception {
        // Given: Create one-way follow
        followService.createFollow(sender.getUsername(), receiver.getUsername());

        // When
        mockMvc.perform(get("/api-v1/member/mutual/{receiver}", receiver.getUsername())
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            // Then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("맞팔로우 여부 조회에 성공했습니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.isMutualFollow").value(false));
    }
}
