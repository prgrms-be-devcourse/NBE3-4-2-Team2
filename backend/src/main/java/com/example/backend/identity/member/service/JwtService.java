package com.example.backend.identity.member.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.backend.entity.MemberEntity;
import com.example.backend.global.util.Ut;


/**
 * <p>Jwt(accessToken)에 관한 처리를 담당하는 서비스</p>
 * <p>현재 서비스 구조상 MemberService에 종속적이므로
 * protected Method로 이루어져 있습니다.</p>
 * @author KimHaeChan
 * @since 25. 2. 3
 * */
@Service
public class JwtService {

	@Value("${custom.jwt.secretKey}")
	private String jwtSecretKey;

	@Value("${custom.accessToken.expirationSeconds}")
	private long accessTokenExpirationSeconds;

	/**
	 * <p>Member 정보로 Jwt Payload를 입력하는 메서드</p>
	 * <p>payload -> { id, username}</p>
	 * @author KimHaeChan
	 * @since 25. 2. 3
	 * */
	protected String genAccessToken(MemberEntity member) {
		long id = member.getId();
		String username = member.getUsername();


		return Ut.jwt.toString(
			jwtSecretKey,
			accessTokenExpirationSeconds,
			Map.of("id", id, "username", username)
		);
	}

	/**
	 * <p>Jwt(accessToken)의 payload를 Map으로 반환하는 메서드</p>
	 * <p>payload -> { id, username}</p>
	 * @author KimHaeChan
	 * @since 25. 2. 3
	 * */
	protected Map<String, Object> payload(String accessToken) {
		Map<String, Object> parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken);

		if (parsedPayload == null) return null;

		long id = (long) (Integer) parsedPayload.get("id");
		String username = (String) parsedPayload.get("username");

		return Map.of("id", id, "username", username);
	}
}
