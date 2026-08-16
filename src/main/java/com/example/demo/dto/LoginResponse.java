package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

	private String token;
	private Long userSeq;
	private String userId;
	private String userNm;
}
