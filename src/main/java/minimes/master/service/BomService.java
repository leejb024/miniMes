package minimes.master.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public List<BomResponse> findHeaders() {
		Map<String, String> itemNames = itemNames();
		return bomRepository.findByUseYnOrderByItemIdAscBomVersionAscBomSeqAsc("Y").stream()
				.collect(Collectors.toMap(
						this::headerKey,
						Function.identity(),
						(first, ignored) -> first,
						LinkedHashMap::new))
				.values()
				.stream()
				.map(bom -> BomResponse.fromHeader(bom, itemNames.get(bom.getItemId())))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<BomItemResponse> findMaterials(String itemId, String bomVersion) {
		return bomRepository.findMaterials(itemId, bomVersion).stream()
				.map(BomItemResponse::from)
				.toList();
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
