package minimes.master.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Item;
import minimes.master.dto.ItemCreateRequest;
import minimes.master.dto.ItemResponse;
import minimes.master.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

	private static final String RAW_MATERIAL_PREFIX = "1";
	private static final String RAW_MATERIAL_TYPE = "6";

	private final ItemRepository itemRepository;

	@Transactional(readOnly = true)
	public List<ItemResponse> findAll() {
		return itemRepository.findAllByOrderByItemIdAsc().stream()
				.map(ItemResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public String nextRawMaterialId() {
		return nextId(RAW_MATERIAL_PREFIX);
	}

	@Transactional
	public ItemResponse createRawMaterial(ItemCreateRequest request, String creId) {
		String itemName = request.getItemName().trim();
		if (!StringUtils.hasText(itemName)) {
			throw new IllegalArgumentException("품목명을 입력하세요.");
		}

		Item item = new Item();
		item.setItemId(nextId(RAW_MATERIAL_PREFIX));
		item.setItemName(itemName);
		item.setItemType(RAW_MATERIAL_TYPE);
		item.setUseYn("Y");
		item.setCreId(StringUtils.hasText(creId) ? creId : "admin");
		item.setCreDt(LocalDateTime.now());
		return ItemResponse.from(itemRepository.save(item));
	}

	private String nextId(String prefix) {
		long max = 0L;
		int width = 0;
		for (Item item : itemRepository.findAllByOrderByItemIdAsc()) {
			String itemId = item.getItemId();
			if (itemId == null || !itemId.startsWith(prefix) || !itemId.chars().allMatch(Character::isDigit)) {
				continue;
			}
			long value = Long.parseLong(itemId);
			if (value >= max) {
				max = value;
				width = itemId.length();
			}
		}
		if (max == 0L) {
			return prefix + "0000001";
		}
		String next = Long.toString(max + 1L);
		if (next.length() < width) {
			return "0".repeat(width - next.length()) + next;
		}
		return next;
	}
}
