package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseCreateRequest {

	@NotBlank(message = "창고ID를 입력하세요.")
	private String warehouseId;

	@NotBlank(message = "창고명을 입력하세요.")
	private String warehouseName;

	private String warehouseType;

	private String useYn;
}
