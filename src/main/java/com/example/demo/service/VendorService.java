package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
