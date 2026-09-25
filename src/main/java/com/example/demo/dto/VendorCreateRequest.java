package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorCreateRequest {

	@NotBlank(message = "거래처ID를 입력하세요.")
	private String vendorId;

	@NotBlank(message = "거래처명을 입력하세요.")
	private String vendorName;

	private String useYn;
}
