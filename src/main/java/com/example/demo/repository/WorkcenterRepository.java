package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.Workcenter;

public interface WorkcenterRepository extends JpaRepository<Workcenter, String> {

	List<Workcenter> findAllByOrderByWorkcenterIdAsc();
}
