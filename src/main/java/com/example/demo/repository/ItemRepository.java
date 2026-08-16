package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.Item;

public interface ItemRepository extends JpaRepository<Item, String> {

	List<Item> findAllByOrderByItemIdAsc();
}
