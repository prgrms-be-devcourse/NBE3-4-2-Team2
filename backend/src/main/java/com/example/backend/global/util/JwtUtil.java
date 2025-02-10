package com.example.backend.global.util;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

/**
 * JWT(JSON Web Token) 관련 유틸리티 클래스.
 * <p>JWT 토큰 생성 및 검증 기능을 제공한다.</p>
 * <p>이 클래스는 인스턴스화할 수 없다.</p>
 * @author k-haechan
 * @since 2025-02-08
 */
public class JwtUtil {

	/**
	 * 인스턴스화 방지를 위한 private 생성자.
	 */
	private JwtUtil() {}

	/**
	 * 주어진 클레임(Claims)과 만료 시간으로 JWT 토큰을 생성한다.
	 *
	 * @param secret         JWT 서명에 사용할 비밀 키 (HMAC 알고리즘을 사용)
	 * @param expireSeconds  토큰 만료 시간 (초 단위)
	 * @param claims         토큰에 포함할 클레임 (key-value 형태)
	 * @return 생성된 JWT 토큰 문자열
	 */
	public static String generateToken(String secret, long expireSeconds, Map<String, Object> claims) {
		Date issuedAt = new Date();
		Date expiration = new Date(issuedAt.getTime() + (expireSeconds * 1000L));
		SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

		return Jwts.builder()
			.claims(claims)
			.issuedAt(issuedAt)
			.expiration(expiration)
			.signWith(secretKey)
			.compact();
	}

	/**
	 * 주어진 JWT 문자열을 파싱하여 페이로드(Claims)를 반환한다.
	 *
	 * @param secret JWT 서명 검증에 사용할 비밀 키
	 * @param jwtStr 검증할 JWT 문자열
	 * @return JWT 페이로드 (Claims) 정보를 포함하는 맵
	 * @throws JwtException JWT 검증에 실패하거나 유효하지 않은 경우 발생
	 */
	public static Map<String, Object> getPayload(String jwtStr, String secret) throws JwtException {
		SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(jwtStr)
			.getPayload();
	}

	public static Date getExpirationDate(String jwtStr, String secret) throws JwtException {
		SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

		try {
			Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(jwtStr)
				.getPayload();

			return claims.getExpiration();  // 만료 시간을 반환
		} catch (SignatureException e) {
			// 토큰이 유효하지 않은 경우 처리
			return null;
		}
	}

}
