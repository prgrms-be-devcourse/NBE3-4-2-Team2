package com.example.backend.identity.security.oauth.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.global.exception.GlobalException;
import com.example.backend.identity.member.exception.MemberErrorCode;
import com.example.backend.identity.security.user.CustomUser;
import com.example.backend.identity.security.oauth.dto.GoogleResponse;
import com.example.backend.identity.security.oauth.dto.NaverResponse;
import com.example.backend.identity.security.oauth.dto.OAuth2Response;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final MemberRepository memberRepository;

	public CustomOAuth2UserService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	@Override // OAuth2UserRequest : OAuth2 프로토콜을 통해 인증된(AccessToken 발급) 사용자의 요청 정보(Scope, clientID, clientSecret 등)를 담고 있는 객체
	public CustomUser loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		// OAuth2USer : OAuth2 인증이 완료된 후, 인증 서버에서 제공한 사용자 정보를 담고 있는 객체, 권한 정보도 포함됨
		OAuth2User oAuth2User = super.loadUser(userRequest);

		// registrationId : 사용자의 요청을 받는 인증서버(ex, google, naver, ...)
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		// OAuth2Reponse : 인증 서버 별로 다른 제공된 사용자 정보를 형식화 하기 위한 커스텀 인터페이스
		OAuth2Response oAuth2Response = null;
		if (registrationId.equals("naver")) {
			oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
		}
		else if (registrationId.equals("google")) {

			String accessToken = userRequest.getAccessToken().getTokenValue();

			String phoneNumber = getPhoneNumber(accessToken);

			oAuth2Response = new GoogleResponse(oAuth2User.getAttributes(), phoneNumber);
		}
		else {
			return null;
		}

		String email = oAuth2Response.getEmail();
		String phoneNumber = oAuth2Response.getPhoneNumber();

		// 전화번호로 회원 조회
		Optional<MemberEntity> member;
		member = memberRepository.findByPhoneNumber(phoneNumber);

		// 이메일로 회원 조회
		if (member.isEmpty()) {
			member = memberRepository.findByEmail(email);
		}

		// 일치하는 정보가 없으면 예외 발생
		if (member.isEmpty()) {
				throw new GlobalException(
					MemberErrorCode.UNAUTHORIZED
				);
		}

		return new CustomUser(member.get(), oAuth2Response);
	}

	private String getPhoneNumber(String accessToken) {
		// 전화번호 요청 api
		String url = "https://people.googleapis.com/v1/people/me?personFields=phoneNumbers";

		// HTTP 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken); // 올바른 인증 방식
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<>(headers);

		RestTemplate restTemplate = new RestTemplate();

		// API 요청
		ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

		Map<String, Object> responseBody = response.getBody();


		if (responseBody != null && responseBody.containsKey("phoneNumbers")) {
			List<Map<String, Object>> phoneNumbersList = (List<Map<String, Object>>) responseBody.get("phoneNumbers");

			// 리스트가 비어있지 않은지 확인
			if (!phoneNumbersList.isEmpty()) {
				String phoneNumber = (String) phoneNumbersList.getFirst().get("value"); // 첫 번째 전화번호 가져오기
				return phoneNumber;
			}
		}
		return null;
	}
}
