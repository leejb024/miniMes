package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.domain.Vendor;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VendorResponse {

	private String vendorId;
	private String useYn;
	private LocalDateTime regDt;
	private LocalDateTime modDt;

	public static VendorResponse from(Vendor vendor) {
		return VendorResponse.builder()
				.vendorId(vendor.getVendorId())
				.useYn(vendor.getUseYn())
				.regDt(vendor.getRegDt())
				.modDt(vendor.getModDt())
				.build();
	}
}
