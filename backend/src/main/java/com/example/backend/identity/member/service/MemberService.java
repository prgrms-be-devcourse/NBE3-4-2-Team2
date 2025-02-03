package com.example.backend.identity.member.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.global.exception.GlobalException;
import com.example.backend.identity.member.exception.MemberErrorCode;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class MemberService {
	private final JwtService jwtService;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;


	public MemberEntity join(String username, String password, String email) {
		memberRepository
			.findByUsername(username)
			.ifPresent(member -> {
				throw new GlobalException(
					MemberErrorCode.CONFLICT_RESOURCE,
					"해당 username은 이미 사용중입니다."
				);
			});
		memberRepository
			.findByEmail(email)
			.ifPresent(member -> {
				throw new GlobalException(
					MemberErrorCode.CONFLICT_RESOURCE,
					"해당 email은 이미 사용중입니다."
				);
			});

		String encodedPassword = passwordEncoder.encode(password);

		MemberEntity member = MemberEntity.builder()
			.username(username)
			.password(encodedPassword)
			.email(email)
			.refreshToken(UUID.randomUUID().toString())
			.build();

		return memberRepository.save(member);
	}

	public Optional<MemberEntity> findByUsername(String username) {
		return memberRepository.findByUsername(username);
	}

	public Optional<MemberEntity> findById(long authorId) {
		return memberRepository.findById(authorId);
	}

	public Optional<MemberEntity> findByRefreshToken(String refreshToken) {
		return memberRepository.findByRefreshToken(refreshToken);
	}

	public String genAccessToken(MemberEntity member) {
		return jwtService.genAccessToken(member);
	}

	public MemberEntity getActorFromAccessToken(String accessToken) {
		Map<String, Object> payload = jwtService.payload(accessToken);

		if (payload == null) return null;

		Long id = (Long) payload.get("id");
		String username = (String) payload.get("username");

		if (id == null || username == null)
			return null;

		MemberEntity actor = new MemberEntity(id, username);

		return actor;
	}


	public boolean matchPassword(MemberEntity member, String password) {
		return passwordEncoder.matches(password, member.getPassword());
	}
}
