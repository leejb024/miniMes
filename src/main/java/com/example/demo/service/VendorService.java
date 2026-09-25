package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.demo.domain.Vendor;
import com.example.demo.dto.VendorCreateRequest;
import com.example.demo.dto.VendorResponse;
import com.example.demo.repository.VendorRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class VendorService {

	private final VendorRepository vendorRepository;

	@Transactional(readOnly = true)
	public List<VendorResponse> findAll() {
		return vendorRepository.findAllByOrderByVendorIdAsc().stream()
				.map(VendorResponse::from)
				.toList();
	}

	@Transactional
	public VendorResponse create(VendorCreateRequest request) {
		String vendorId = request.getVendorId().trim();
		if (vendorRepository.existsById(vendorId)) {
			throw new IllegalArgumentException("이미 존재하는 거래처ID입니다.");
		}
		if (!StringUtils.hasText(request.getVendorName())) {
			throw new IllegalArgumentException("거래처명을 입력하세요.");
		}
		LocalDateTime now = LocalDateTime.now();
		Vendor vendor = new Vendor();
		vendor.setVendorId(vendorId);
		vendor.setVendorName(request.getVendorName().trim());
		vendor.setUseYn(StringUtils.hasText(request.getUseYn()) ? request.getUseYn().trim() : "Y");
		vendor.setRegDt(now);
		vendor.setModDt(now);
		return VendorResponse.from(vendorRepository.save(vendor));
	}
}
