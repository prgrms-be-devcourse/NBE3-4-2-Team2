package com.example.backend.identity.security.user;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.backend.entity.MemberEntity;
import com.example.backend.identity.security.oauth.dto.OAuth2Response;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUser implements OAuth2User, UserDetails {
	private final MemberEntity memberEntity;
	private final OAuth2Response oAuth2Response;

	@Override
	public String getPassword() {
		return memberEntity.getPassword();
	}

	@Override
	public String getUsername() {
		return memberEntity.getUsername();
	}

	@Override
	public Map<String, Object> getAttributes() { // oAuth로 제공받은 정보들. 굳이 사용할 필요 없음?
		return Map.of();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return memberEntity.getAuthorities();
	}

	@Override
	public String getName() { // 우리 프로그램 특성상 필요 없기도 함
		return oAuth2Response.getName();
	}

	public long getId() {
		return memberEntity.getId();
	}
}
