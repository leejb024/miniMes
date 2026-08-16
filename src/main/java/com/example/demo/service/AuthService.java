package com.example.demo.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.demo.domain.User;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtils;
import com.example.demo.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;
	private final UserRepository userRepository;

	public LoginResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUserId(), request.getPassword()));

		UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
		String token = jwtUtils.generateToken(principal.getUsername());

		return LoginResponse.builder()
				.token(token)
				.userSeq(principal.getUserSeq())
				.userId(principal.getUsername())
				.userNm(principal.getUserNm())
				.build();
	}

	public UserResponse currentUser(String userId) {
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
		return UserResponse.builder()
				.userSeq(user.getUserSeq())
				.userId(user.getUserId())
				.userNm(user.getUserNm())
				.build();
	}
}
