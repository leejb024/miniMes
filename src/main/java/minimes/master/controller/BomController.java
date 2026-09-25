package minimes.master.controller;

import java.util.List;

import minimes.master.dto.BomItemResponse;
import minimes.master.dto.BomResponse;
import minimes.master.service.BomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BomController {

	private final BomService bomService;

	@GetMapping
	public List<BomResponse> list() {
		return bomService.findHeaders();
	}

	@GetMapping("/materials")
	public List<BomItemResponse> materials(
			@RequestParam String itemId,
			@RequestParam String bomVersion) {
		return bomService.findMaterials(itemId, bomVersion);
	}
}
