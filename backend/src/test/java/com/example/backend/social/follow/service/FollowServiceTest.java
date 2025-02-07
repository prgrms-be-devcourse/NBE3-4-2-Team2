package com.example.backend.social.follow.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.backend.entity.FollowEntity;
import com.example.backend.entity.FollowRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.social.follow.dto.CreateFollowResponse;
import com.example.backend.social.follow.exception.FollowErrorCode;
import com.example.backend.social.follow.exception.FollowException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

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

	@BeforeEach
	public void setup() {
		// 테스트 전에 데이터 초기화
		followRepository.deleteAll();
		memberRepository.deleteAll();

		// 시퀀스 초기화 (테스트 데이터 재 생성시 아이디 값이 올라가기 때문)
		entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE follow ALTER COLUMN id RESTART WITH 1").executeUpdate();

		// 테스트용 Followee 멤버 추가
		MemberEntity member1 = MemberEntity.builder()
			.username("testSender")
			.email("testSender@gmail.com")
			.password("testPassword")
			.refreshToken(UUID.randomUUID().toString())
			.build();
		testSender = memberRepository.save(member1);

		// 테스트용 Follower 멤버 추가
		MemberEntity member2 = MemberEntity.builder()
			.username("testReceiver")
			.email("testReceiver@gmail.com")
			.password("testPassword")
			.refreshToken(UUID.randomUUID().toString())
			.build();
		testReceiver = memberRepository.save(member2);
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
		FollowEntity follow = followRepository.findById(createResponse.id())
			.orElseThrow(() -> new FollowException(FollowErrorCode.FOLLOW_NOT_FOUND));

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
		assertThrows(FollowException.class, () -> {
			followService.createFollow(nonExistSenderId, receiverId);
		}, FollowErrorCode.MEMBER_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("4. 존재하지 않는 receiver의 팔로우 요청 테스트")
	public void t004() {
		// Given
		Long senderId = testSender.getId();
		Long nonExistReceiverId = 999L;

		// When & Then
		assertThrows(FollowException.class, () -> {
			followService.createFollow(senderId, nonExistReceiverId);
		}, FollowErrorCode.MEMBER_NOT_FOUND.getMessage());
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
		assertThrows(FollowException.class, () -> {
			followService.createFollow(senderId2, receiverId2);
		}, FollowErrorCode.ALREADY_FOLLOWED.getMessage());

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
		assertThrows(FollowException.class, () -> {
			followService.deleteFollow(nonExistFollowId, senderId, receiverId);
		}, FollowErrorCode.FOLLOW_NOT_FOUND.getMessage());

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
		Long followId = createResponse.id();
		Long anotherSenderId = 999L;
		Long correctReceiverId = createResponse.receiverId();

		// When & Then Second
		assertThrows(FollowException.class, () -> {
			followService.deleteFollow(followId, anotherSenderId, correctReceiverId);
		}, FollowErrorCode.SENDER_MISMATCH.getMessage());
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
		Long followId = createResponse.id();
		Long correctSenderId = createResponse.senderId();
		Long anotherReceiverId = 999L;

		// When & Then Second
		assertThrows(FollowException.class, () -> {
			followService.deleteFollow(followId, correctSenderId, anotherReceiverId);
		}, FollowErrorCode.RECEIVER_MISMATCH.getMessage());
	}
}
