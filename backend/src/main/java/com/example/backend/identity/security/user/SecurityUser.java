package com.example.backend.identity.security.user;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;

/**
 * <p>User에 id를 추가하여 서비스 동안 성능을 최적화 하기위한 클래스 </p>
 * @author KimHaeChan
 * @since 25. 2. 3
 * */
@Getter
public class SecurityUser extends User {
	private long id;

	public SecurityUser(
		long id,
		String username,
		String password,
		Collection<? extends GrantedAuthority> authorities
	) {
		super(username, password, authorities);
		this.id = id;
	}
}
