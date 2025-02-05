package com.example.backend.global.requestScope;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.example.backend.entity.MemberEntity;
import com.example.backend.identity.security.user.SecurityUser;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequestScope
@Component
@RequiredArgsConstructor
public class Rq {
	private final HttpServletRequest req;
	private final HttpServletResponse resp;

	public void setLogin(MemberEntity member) {
		UserDetails user = new SecurityUser(
			member.getId(),
			member.getUsername(),
			"",
			member.getAuthorities()
		);

		Authentication authentication = new UsernamePasswordAuthenticationToken(
			user,
			user.getPassword(),
			user.getAuthorities()
		);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	public void setCookie(String name, String value) {
		ResponseCookie cookie = ResponseCookie.from(name, value)
			.path("/")
			.domain("localhost")
			.sameSite("Strict")
			.secure(true)
			.httpOnly(true)
			.build();
		resp.addHeader("Set-Cookie", cookie.toString());
	}

	public String getCookieValue(String name) {
		return Optional
			.ofNullable(req.getCookies())
			.stream() // 1 ~ 0
			.flatMap(Arrays::stream)
			.filter(cookie -> cookie.getName().equals(name))
			.map(Cookie::getValue)
			.findFirst()
			.orElse(null);
	}

	public void deleteCookie(String name) {
		ResponseCookie cookie = ResponseCookie.from(name, null)
			.path("/")
			.domain("localhost")
			.sameSite("Strict")
			.secure(true)
			.httpOnly(true)
			.maxAge(0)
			.build();

		resp.addHeader("Set-Cookie", cookie.toString());
	}

	public void setHeader(String name, String value) {
		resp.setHeader(name, value);
	}

	public String getHeader(String name) {
		return req.getHeader(name);
	}
}
