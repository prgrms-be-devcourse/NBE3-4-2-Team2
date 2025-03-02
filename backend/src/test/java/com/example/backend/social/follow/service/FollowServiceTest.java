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

import com.example.backend.entity.FollowEntity;
import com.example.backend.entity.FollowRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.global.event.FollowEventListener;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.social.exception.SocialErrorCode;
import com.example.backend.social.exception.SocialException;
import com.example.backend.social.follow.dto.CreateFollowResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

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
	private FollowRepository followRepository;

	@Autowired
	private MemberRepository memberRepository;

	private MemberEntity testSender;
	private MemberEntity testReceiver;
	@Autowired
	private MemberService memberService;
	@MockitoBean
	FollowEventListener followEventListener;

	@BeforeEach
	public void setup() {
		// 테스트 전에 데이터 초기화
		followRepository.deleteAll();
		memberRepository.deleteAll();

		// 시퀀스 초기화 (테스트 데이터 재 생성시 아이디 값이 올라가기 때문)
		entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE follow ALTER COLUMN id RESTART WITH 1").executeUpdate();

		// 테스트용 Followee 멤버 추가
		testSender = memberService.join("testSender","testPassword","testSender@gmail.com");

		// 테스트용 Follower 멤버 추가
		testReceiver = memberService.join("testReceiver","testPassword","testReceiver@gmail.com");

	}

	@Test
	@DisplayName("1. 팔로우 요청 테스트")
	public void t001() throws Exception {
		// Given First
		Long senderId = testSender.getId();
		Long receiverId = testReceiver.getId();

		// When First
		CreateFollowResponse createResponse = followService.createFollow(senderId, receiverId);

		// Then First
		assertNotNull(createResponse);
		assertEquals(senderId, createResponse.senderId());
		assertEquals(receiverId, createResponse.receiverId());

		// When Second
		MemberEntity sender = memberRepository.findById(senderId)
			.orElseThrow(() -> new EntityNotFoundException("팔로워를 찾을 수 없습니다."));
		Long senderFollowerCount = sender.getFollowerCount();

		MemberEntity receiver = memberRepository.findById(receiverId)
			.orElseThrow(() -> new EntityNotFoundException("팔로위를 찾을 수 없습니다."));
		Long receiverFolloweeCount = receiver.getFolloweeCount();

		// Then Second
		assertNotNull(sender);
		assertNotNull(receiver);
		assertEquals(1L, senderFollowerCount);
		assertEquals(1L, receiverFolloweeCount);
	}

	@Test
	@DisplayName("2. 팔로우 취소 요청 테스트")
	public void t002() throws Exception {
		// Given First
		Long senderId = testSender.getId();
		Long receiverId = testReceiver.getId();

		// When First
		CreateFollowResponse createResponse = followService.createFollow(senderId, receiverId);

		// Then First
		assertNotNull(createResponse);
		assertEquals(senderId, createResponse.senderId());
		assertEquals(receiverId, createResponse.receiverId());

		// Given Second
		FollowEntity follow = followRepository.findById(createResponse.followId())
			.orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND));

		// When & Then Second
		followService.deleteFollow(
			follow.getId(), follow.getSender().getId(), follow.getReceiver().getId()
		);

		// When Third
		MemberEntity sender = memberRepository.findById(senderId)
			.orElseThrow(() -> new EntityNotFoundException("팔로워를 찾을 수 없습니다."));
		Long senderFollowerCount = sender.getFollowerCount();

		MemberEntity receiver = memberRepository.findById(receiverId)
			.orElseThrow(() -> new EntityNotFoundException("팔로위를 찾을 수 없습니다."));
		Long receiverFolloweeCount = receiver.getFolloweeCount();

		// Then Third
		assertNotNull(sender);
		assertNotNull(receiver);
		assertEquals(0L, senderFollowerCount);
		assertEquals(0L, receiverFolloweeCount);
	}

	@Test
	@DisplayName("3. 존재하지 않는 sender의 팔로우 요청 테스트")
	public void t003() {
		// Given
		Long nonExistSenderId = 999L;
		Long receiverId = testReceiver.getId();

		// When & Then
		SocialException exception = assertThrows(SocialException.class, () -> {
			followService.createFollow(nonExistSenderId, receiverId);
		});
		assertEquals(SocialErrorCode.NOT_FOUND, exception.getErrorCode());
		assertEquals("요청측 클라이언트 검증에 실패했습니다.", exception.getMessage());

	}

	@Test
	@DisplayName("4. 존재하지 않는 receiver의 팔로우 요청 테스트")
	public void t004() {
		// Given
		Long senderId = testSender.getId();
		Long nonExistReceiverId = 999L;

		// When & Then
		SocialException exception = assertThrows(SocialException.class, () -> {
			followService.createFollow(senderId, nonExistReceiverId);
		});
		assertEquals(SocialErrorCode.NOT_FOUND, exception.getErrorCode());
		assertEquals("응답측 클라이언트 검증에 실패했습니다.", exception.getMessage());
	}

	@Test
	@DisplayName("5. 이미 팔로우된 상태에서 중복 팔로우 테스트")
	public void t005() {
		// Given First
		Long senderId = testSender.getId();
		Long receiverId = testReceiver.getId();

		// When First
		CreateFollowResponse createResponse = followService.createFollow(senderId, receiverId);

		// Then First
		assertNotNull(createResponse);
		assertEquals(senderId, createResponse.senderId());
		assertEquals(receiverId, createResponse.receiverId());

		// Given Secend
		Long senderId2 = createResponse.senderId();
		Long receiverId2 = createResponse.receiverId();

		// When & Then Second
		assertThrows(SocialException.class, () -> {
			followService.createFollow(senderId2, receiverId2);
		}, SocialErrorCode.ALREADY_EXISTS.getMessage());

		// When Third
		MemberEntity sender = memberRepository.findById(senderId2)
			.orElseThrow(() -> new EntityNotFoundException("팔로워를 찾을 수 없습니다."));
		Long senderFollowerCount = sender.getFollowerCount();

		MemberEntity receiver = memberRepository.findById(receiverId2)
			.orElseThrow(() -> new EntityNotFoundException("팔로위를 찾을 수 없습니다."));
		Long receiverFolloweeCount = receiver.getFolloweeCount();

		// Then Third
		assertNotNull(sender);
		assertNotNull(receiver);
		assertEquals(1L, senderFollowerCount);
		assertEquals(1L, receiverFolloweeCount);
	}

	@Test
	@DisplayName("6. 팔로우가 아닌 상태에서 팔로우 취소 테스트")
	public void t006() {
		// Given First
		Long nonExistFollowId = 1L;
		Long senderId = testSender.getId();
		Long receiverId = testReceiver.getId();

		// When & Then First
		SocialException exception = assertThrows(SocialException.class, () -> {
			followService.deleteFollow(nonExistFollowId, senderId, receiverId);
		});
		assertEquals(SocialErrorCode.NOT_FOUND, exception.getErrorCode());
		assertEquals("팔로우 확인에 실패했습니다.", exception.getMessage());

		// When Second
		Long senderFollowerCount = testSender.getFollowerCount();
		Long receiverFolloweeCount = testReceiver.getFolloweeCount();

		// Then Second
		assertEquals(0L, senderFollowerCount);
		assertEquals(0L, receiverFolloweeCount);
	}

	@Test
	@DisplayName("7. 팔로우 취소 요청을 다른 멤버가 요청하는 테스트")
	public void t007() {
		// Given First
		Long senderId = testSender.getId();
		Long receiverId = testReceiver.getId();

		// When First
		CreateFollowResponse createResponse = followService.createFollow(senderId, receiverId);

		// Then First
		assertNotNull(createResponse);
		assertEquals(senderId, createResponse.senderId());
		assertEquals(receiverId, createResponse.receiverId());

		// Given Secend
		Long followId = createResponse.followId();
		Long anotherSenderId = 999L;
		Long correctReceiverId = createResponse.receiverId();

		// When & Then Second
		assertThrows(SocialException.class, () -> {
			followService.deleteFollow(followId, anotherSenderId, correctReceiverId);
		}, SocialErrorCode.DATA_MISMATCH.getMessage());
	}

	@Test
	@DisplayName("8. 다른 멤버의 ID로 취소 요청 테스트")
	public void t008() {
		// Given First
		Long senderId = testSender.getId();
		Long receiverId = testReceiver.getId();

		// When First
		CreateFollowResponse createResponse = followService.createFollow(senderId, receiverId);

		// Then First
		assertNotNull(createResponse);
		assertEquals(senderId, createResponse.senderId());
		assertEquals(receiverId, createResponse.receiverId());

		// Given Secend
		Long followId = createResponse.followId();
		Long correctSenderId = createResponse.senderId();
		Long anotherReceiverId = 999L;

		// When & Then Second
		assertThrows(SocialException.class, () -> {
			followService.deleteFollow(followId, correctSenderId, anotherReceiverId);
		}, SocialErrorCode.DATA_MISMATCH.getMessage());
	}

	@Test
	@DisplayName("9. 맞팔로우 적용 확인 테스트")
	public void t009() {
		// Given First
		Long senderId = testSender.getId();
		Long receiverId = testReceiver.getId();

		// When First
		followService.createFollow(senderId, receiverId);
		followService.createFollow(receiverId, senderId);

		MemberEntity sender = memberRepository.findById(senderId)
			.orElseThrow(() -> new EntityNotFoundException("팔로워를 찾을 수 없습니다."));
		Long senderFollowerCount = sender.getFollowerCount();
		Long senderFolloweeCount = sender.getFolloweeCount();

		MemberEntity receiver = memberRepository.findById(receiverId)
			.orElseThrow(() -> new EntityNotFoundException("팔로위를 찾을 수 없습니다."));
		Long receiverFollowerCount = receiver.getFollowerCount();
		Long receiverFolloweeCount = receiver.getFolloweeCount();

		// Then First
		assertEquals(1L, senderFollowerCount);
		assertEquals(1L, senderFolloweeCount);
		assertEquals(1L, receiverFollowerCount);
		assertEquals(1L, receiverFolloweeCount);

		// When Second
		boolean isMutualFollow = followService.findMutualFollow(senderId, receiverId);

		// Then Second
		assertTrue(isMutualFollow);
	}
}
