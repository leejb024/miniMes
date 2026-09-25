package minimes.master.controller;

import java.util.List;

import minimes.master.dto.ItemResponse;
import minimes.master.service.ItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@GetMapping
	public List<ItemResponse> list() {
		return itemService.findAll();
	}
}
