package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.Process;

public interface ProcessRepository extends JpaRepository<Process, String> {

	List<Process> findAllByOrderByProcessIdAsc();
}
