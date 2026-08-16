package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtUtilsTest {

	@Test
	void generateAndParseToken() {
		JwtUtils jwtUtils = new JwtUtils("miniMes-test-jwt-secret-key-change-in-production", 3600000);

		String token = jwtUtils.generateToken("admin");

		assertThat(token).isNotBlank();
		assertThat(jwtUtils.isValid(token)).isTrue();
		assertThat(jwtUtils.getUserId(token)).isEqualTo("admin");
	}
}
