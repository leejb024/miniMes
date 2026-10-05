package minimes.master.service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Bom;
import minimes.master.domain.Item;
import minimes.master.dto.BomItemResponse;
import minimes.master.dto.BomResponse;
import minimes.master.repository.BomRepository;
import minimes.master.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BomService {

	private final BomRepository bomRepository;
	private final ItemRepository itemRepository;

	@Transactional(readOnly = true)
	public List<BomResponse> findHeaders(String itemId, String itemName, boolean productsOnly) {
		Map<String, String> itemNames = itemNames();
		String itemIdKeyword = normalize(itemId);
		String itemNameKeyword = normalize(itemName);
		return bomRepository.findByUseYnOrderByItemIdAscBomVersionAscBomSeqAsc("Y").stream()
				.filter(bom -> !productsOnly || isProductCode(bom.getItemId()))
				.collect(Collectors.toMap(
						this::headerKey,
						Function.identity(),
						(first, ignored) -> first,
						LinkedHashMap::new))
				.values()
				.stream()
				.filter(bom -> contains(bom.getItemId(), itemIdKeyword))
				.filter(bom -> contains(itemNames.get(bom.getItemId()), itemNameKeyword))
				.map(bom -> BomResponse.fromHeader(bom, itemNames.get(bom.getItemId())))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<BomItemResponse> findMaterials(String itemId, String bomVersion) {
		return bomRepository.findMaterials(itemId, bomVersion).stream()
				.map(BomItemResponse::from)
				.toList();
	}

	private boolean isProductCode(String itemId) {
		return itemId != null && itemId.startsWith("2");
	}

	private String normalize(String value) {
		return StringUtils.hasText(value) ? value.trim().toLowerCase() : "";
	}

	private boolean contains(String value, String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return true;
		}
		return value != null && value.toLowerCase().contains(keyword);
	}

	private String headerKey(Bom bom) {
		return String.join("|",
				nullToEmpty(bom.getItemId()),
				nullToEmpty(bom.getBomVersion()));
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private Map<String, String> itemNames() {
		return itemRepository.findAll().stream()
				.filter(item -> item.getItemId() != null)
				.collect(Collectors.toMap(Item::getItemId, Item::getItemName, (left, right) -> left));
	}
}
