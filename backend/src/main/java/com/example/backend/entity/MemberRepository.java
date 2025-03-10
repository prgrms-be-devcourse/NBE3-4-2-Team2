package com.example.backend.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
	Optional<MemberEntity> findByUsername(String username);

	// Optional<MemberEntity> findByRefreshToken(String refreshToken);

	Optional<MemberEntity> findByEmail(String email);

	Optional<MemberEntity> findByPhoneNumber(String phoneNumber);
}
