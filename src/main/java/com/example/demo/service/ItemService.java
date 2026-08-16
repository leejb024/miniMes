package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ItemResponse;
import com.example.demo.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

	private final ItemRepository itemRepository;

	@Transactional(readOnly = true)
	public List<ItemResponse> findAll() {
		return itemRepository.findAllByOrderByItemIdAsc().stream()
				.map(ItemResponse::from)
				.toList();
	}
}
