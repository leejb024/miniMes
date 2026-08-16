package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, String> {

	List<Vendor> findAllByOrderByVendorIdAsc();
}
