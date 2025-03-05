package com.example.backend.identity.security.jwt;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.example.backend.global.util.JwtUtil;
import com.example.backend.identity.security.user.CustomUser;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final RedisTemplate<String, String> redisTemplate;

	@Value("${custom.jwt.refreshToken.secretKey}")
	private String refreshTokenSecretKey;

	@Getter
	@Value("${custom.jwt.refreshToken.expirationSeconds}")
	private long refreshTokenExpirationSeconds;

	public String genRefreshToken(String username, long id) {
		return JwtUtil.generateToken(
			username,
			Map.of("id", id),
			refreshTokenSecretKey,
			refreshTokenExpirationSeconds
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

	public void genRefreshToken(CustomUser loginUser, HttpServletResponse response) {
		String refreshToken = genRefreshToken(loginUser.getUsername(), loginUser.getId());

		ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
			.path("/")
			.maxAge(refreshTokenExpirationSeconds)
			.httpOnly(true)
			.secure(false) // todo : https로 변경하면 이부분도 변경
			.sameSite("Lax") // GET 요청에 대해서만 쿠키 전송
			.build();

		response.addHeader("Set-Cookie", refreshTokenCookie.toString());
	}

	public String getSubject(String refreshToken) {
		return JwtUtil.getSubject(refreshToken, refreshTokenSecretKey);
	}
}

