package com.example.backend.social.follow.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.global.event.FollowEventListener;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.social.exception.SocialErrorCode;
import com.example.backend.social.exception.SocialException;
import com.example.backend.social.follow.dto.FollowResponse;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FollowServiceTest {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FollowService followService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @MockitoBean
    FollowEventListener followEventListener;

    private MemberEntity testSender;
    private MemberEntity testReceiver;

    @BeforeEach
    public void setup() {
        // 테스트 전에 데이터 초기화
        memberRepository.deleteAll();

        // 시퀀스 초기화 (테스트 데이터 재 생성시 아이디 값이 올라가기 때문)
        entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();

        // 테스트용 Sender 멤버 추가
        testSender = memberService.join("testSender", "testPassword", "testSender@gmail.com");

        // 테스트용 Receiver 멤버 추가
        testReceiver = memberService.join("testReceiver", "testPassword", "testReceiver@gmail.com");
    }

    @Test
    @DisplayName("1. 팔로우 요청 테스트")
    public void t001() throws Exception {
        // Given
        String senderUsername = testSender.getUsername();
        String receiverUsername = testReceiver.getUsername();

        // When
        FollowResponse response = followService.createFollow(senderUsername, receiverUsername);

        // Then
        assertNotNull(response);
        assertEquals(senderUsername, response.senderUsername());
        assertEquals(receiverUsername, response.receiverUsername());
        assertNotNull(response.timestamp());

        // Also check if follow relationship is established in both entities
        MemberEntity sender = memberRepository.findByUsername(senderUsername)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        MemberEntity receiver = memberRepository.findByUsername(receiverUsername)
            .orElseThrow(() -> new RuntimeException("Receiver not found"));

        assertTrue(sender.getFollowingList().contains(receiver));
        assertTrue(receiver.getFollowerList().contains(sender));
    }

    @Test
    @DisplayName("2. 팔로우 취소 요청 테스트")
    public void t002() throws Exception {
        // Given
        String senderUsername = testSender.getUsername();
        String receiverUsername = testReceiver.getUsername();

        // Create follow relationship first
        followService.createFollow(senderUsername, receiverUsername);

        // When
        FollowResponse response = followService.deleteFollow(senderUsername, receiverUsername);

        // Then
        assertNotNull(response);
        assertEquals(senderUsername, response.senderUsername());
        assertEquals(receiverUsername, response.receiverUsername());
        assertNotNull(response.timestamp());

        // Also check if follow relationship is removed in both entities
        MemberEntity sender = memberRepository.findByUsername(senderUsername)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        MemberEntity receiver = memberRepository.findByUsername(receiverUsername)
            .orElseThrow(() -> new RuntimeException("Receiver not found"));

        assertFalse(sender.getFollowingList().contains(receiver));
        assertFalse(receiver.getFollowerList().contains(sender));
    }

    @Test
    @DisplayName("3. 존재하지 않는 sender의 팔로우 요청 테스트")
    public void t003() {
        // Given
        String nonExistentSenderUsername = "nonExistentUser";
        String receiverUsername = testReceiver.getUsername();

        // When & Then
        SocialException exception = assertThrows(SocialException.class, () -> {
            followService.createFollow(nonExistentSenderUsername, receiverUsername);
        });
        assertEquals(SocialErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("요청측 회원 검증에 실패했습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("4. 존재하지 않는 receiver의 팔로우 요청 테스트")
    public void t004() {
        // Given
        String senderUsername = testSender.getUsername();
        String nonExistentReceiverUsername = "nonExistentUser";

        // When & Then
        SocialException exception = assertThrows(SocialException.class, () -> {
            followService.createFollow(senderUsername, nonExistentReceiverUsername);
        });
        assertEquals(SocialErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("응답측 회원 검증에 실패했습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("5. 이미 팔로우된 상태에서 중복 팔로우 테스트")
    public void t005() {
        // Given
        String senderUsername = testSender.getUsername();
        String receiverUsername = testReceiver.getUsername();

        // First follow
        followService.createFollow(senderUsername, receiverUsername);

        // When & Then for second follow attempt
        SocialException exception = assertThrows(SocialException.class, () -> {
            followService.createFollow(senderUsername, receiverUsername);
        });
        assertEquals(SocialErrorCode.ALREADY_EXISTS, exception.getErrorCode());
        assertEquals("이미 팔로우 상태입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("6. 팔로우가 아닌 상태에서 팔로우 취소 테스트")
    public void t006() {
        // Given
        String senderUsername = testSender.getUsername();
        String receiverUsername = testReceiver.getUsername();

        // When & Then
        SocialException exception = assertThrows(SocialException.class, () -> {
            followService.deleteFollow(senderUsername, receiverUsername);
        });
        assertEquals(SocialErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("팔로우 관계를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("7. 맞팔로우 확인 테스트 - 맞팔로우 상태")
    public void t007() {
        // Given
        String senderUsername = testSender.getUsername();
        String receiverUsername = testReceiver.getUsername();

        // Create mutual follow
        followService.createFollow(senderUsername, receiverUsername);
        followService.createFollow(receiverUsername, senderUsername);

        // When
        boolean isMutual = followService.isMutualFollow(senderUsername, receiverUsername);

        // Then
        assertTrue(isMutual);
    }

    @Test
    @DisplayName("8. 맞팔로우 확인 테스트 - 단방향 팔로우 상태")
    public void t008() {
        // Given
        String senderUsername = testSender.getUsername();
        String receiverUsername = testReceiver.getUsername();

        // Create one-way follow
        followService.createFollow(senderUsername, receiverUsername);

        // When
        boolean isMutual = followService.isMutualFollow(senderUsername, receiverUsername);

        // Then
        assertFalse(isMutual);
    }

    @Test
    @DisplayName("9. 맞팔로우 확인 테스트 - 팔로우 관계 없음")
    public void t009() {
        // Given
        String senderUsername = testSender.getUsername();
        String receiverUsername = testReceiver.getUsername();

        // When
        boolean isMutual = followService.isMutualFollow(senderUsername, receiverUsername);

        // Then
        assertFalse(isMutual);
    }
}
