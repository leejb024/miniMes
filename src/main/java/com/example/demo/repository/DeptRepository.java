package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.Dept;

public interface DeptRepository extends JpaRepository<Dept, String> {

	List<Dept> findAllByOrderByDeptIdAsc();
}
