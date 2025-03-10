package com.example.backend.identity.member.service;

import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.global.exception.GlobalException;
import com.example.backend.identity.member.exception.MemberErrorCode;
import com.example.backend.identity.security.user.CustomUser;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class MemberService {
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
			.build();

		return memberRepository.save(member);
	}

	public CustomUser login(String username, String password) {
		MemberEntity member = memberRepository
			.findByUsername(username)
			.orElseThrow(() ->
				new UsernameNotFoundException("해당 username을 가진 회원을 찾을 수 없습니다.")
			);

		if (!passwordEncoder.matches(password, member.getPassword())) {
			throw
				new BadCredentialsException("비밀번호가 일치하지 않습니다.");
		}

		CustomUser loginUser = new CustomUser(
			member,
			null
		);

		loginUser.setLogin();

		return loginUser;
	}

	public Optional<MemberEntity> findByUsername(String username) {
		return memberRepository.findByUsername(username);
	}

	public Optional<MemberEntity> findById(long authorId) {
		return memberRepository.findById(authorId);
	}

}
