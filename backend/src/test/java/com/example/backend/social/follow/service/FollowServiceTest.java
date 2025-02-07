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

		// Given Second - Follow 엔티티 가져오기
		FollowEntity follow = followRepository.findById(createResponse.id())
			.orElseThrow(() -> new FollowException(FollowErrorCode.FOLLOW_NOT_FOUND));

		// When Second - 팔로우 취소 요청
		followService.deleteFollow(
			follow.getId(), follow.getSender().getId(), follow.getReceiver().getId()
		);

		// Then Second - 팔로우 상태 검증
		MemberEntity sender = memberRepository.findById(senderId)
			.orElseThrow(() -> new EntityNotFoundException("팔로워를 찾을 수 없습니다."));
		Long senderFollowerCount = sender.getFollowerCount();

		MemberEntity receiver = memberRepository.findById(receiverId)
			.orElseThrow(() -> new EntityNotFoundException("팔로위를 찾을 수 없습니다."));
		Long receiverFolloweeCount = receiver.getFolloweeCount();

		assertNotNull(sender);
		assertNotNull(receiver);
		assertEquals(0L, senderFollowerCount);
		assertEquals(0L, receiverFolloweeCount);
	}
}
