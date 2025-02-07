package com.example.backend.social.follow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.entity.FollowRepository;
import com.example.backend.entity.MemberRepository;

/**
 * 팔로우 서비스
 * 팔로우 서비스 관련 로직 구현
 *
 * @author Metronon
 * @since 2025-02-07
 */
@Service
public class FollowService {
	private final FollowRepository followRepository;
	private final MemberRepository memberRepository;

	@Autowired
	public FollowService(FollowRepository followRepository, MemberRepository memberRepository) {
		this.followRepository = followRepository;
		this.memberRepository = memberRepository;
	}
}
