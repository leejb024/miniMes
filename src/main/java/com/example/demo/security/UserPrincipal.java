package com.example.demo.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.domain.User;

public class UserPrincipal implements UserDetails {

	private final Long userSeq;
	private final String userId;
	private final String userNm;
	private final String password;
	private final boolean enabled;

	public UserPrincipal(User user) {
		this.userSeq = user.getUserSeq();
		this.userId = user.getUserId();
		this.userNm = user.getUserNm();
		this.password = user.getPassword();
		this.enabled = user.isActive();
	}

	public Long getUserSeq() {
		return userSeq;
	}

	public String getUserNm() {
		return userNm;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return userId;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
