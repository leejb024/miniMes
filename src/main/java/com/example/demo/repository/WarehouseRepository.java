package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, String> {

	List<Warehouse> findAllByOrderByWarehouseIdAsc();
}
