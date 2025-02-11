package com.example.backend.identity.security.jwt;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.backend.global.util.JwtUtil;
import com.example.backend.identity.security.user.CustomUser;

import io.jsonwebtoken.JwtException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final RedisTemplate<String, String> redisTemplate;
	private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60; // 7일 (초 단위)

	@Value("${custom.jwt.refreshToken.secretKey}")
	private String refreshTokenSecretKey;

	@Getter
	@Value("${custom.jwt.refreshToken.expirationSeconds}")
	private long refreshTokenExpirationSeconds;

	public String genRefreshToken(CustomUser customUser) {
		return JwtUtil.generateToken(
			refreshTokenSecretKey,
			refreshTokenExpirationSeconds,
			Map.of("id", customUser.getId(), "username", customUser.getUsername())
		);
	}


	public Map<String, Object> getRefreshTokenPayload(String refreshToken) throws JwtException {
		return JwtUtil.getPayload(refreshToken, refreshTokenSecretKey);
	}


	public void addToBlacklist(String token) {
		long expiration = getTokenExpiration(token);
		redisTemplate.opsForValue().set(token, "blacklisted", expiration, TimeUnit.MILLISECONDS);
	}

	public boolean isBlacklisted(String token) {
		return redisTemplate.hasKey(token);
	}

	private long getTokenExpiration(String token) {
		// JWT에서 만료 시간 추출
		Date expiredDate = JwtUtil.getExpirationDate(token, refreshTokenSecretKey);

		if (expiredDate != null) {
			return expiredDate.getTime() - System.currentTimeMillis();
		}
		return 0L;
	}

}

