package minimes.master.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import minimes.master.dto.ItemCreateRequest;
import minimes.master.dto.ItemResponse;
import minimes.master.service.ItemService;
import minimes.security.UserPrincipal;

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

	@GetMapping("/next-id")
	public Map<String, String> nextId(@RequestParam String kind) {
		return Map.of("itemId", itemService.nextId(kind));
	}

	@GetMapping("/next-raw-id")
	public Map<String, String> nextRawMaterialId() {
		return Map.of("itemId", itemService.nextRawMaterialId());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ItemResponse create(
			@RequestBody ItemCreateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		String creId = principal == null ? "admin" : principal.getUsername();
		return itemService.create(request, creId);
	}

	@DeleteMapping("/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String itemId) {
		itemService.delete(itemId);
	}
}
