package minimes.master.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Item;
import minimes.master.dto.ItemCreateRequest;
import minimes.master.dto.ItemResponse;
import minimes.master.repository.BomRepository;
import minimes.master.repository.ItemRepository;
import minimes.production.repository.WorkOrderRepository;
import minimes.purchase.repository.PurchaseLotRepository;
import minimes.purchase.repository.PurchaseOrderItemRepository;
import minimes.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

	private static final String RAW_MATERIAL_PREFIX = "1";
	private static final String RAW_MATERIAL_TYPE = "6";
	private static final String PRODUCT_PREFIX = "2";
	private static final String PRODUCT_TYPE = "2";
	private static final String KIND_RAW = "RAW";
	private static final String KIND_PRODUCT = "PRODUCT";

	private final ItemRepository itemRepository;
	private final PurchaseOrderItemRepository purchaseOrderItemRepository;
	private final PurchaseLotRepository purchaseLotRepository;
	private final StockRepository stockRepository;
	private final WorkOrderRepository workOrderRepository;
	private final BomRepository bomRepository;

	@Transactional(readOnly = true)
	public List<ItemResponse> findAll() {
		return itemRepository.findAllByOrderByItemIdAsc().stream()
				.map(ItemResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public String nextRawMaterialId() {
		return nextItemId(RAW_MATERIAL_PREFIX);
	}

	@Transactional(readOnly = true)
	public String nextId(String kind) {
		return nextItemId(prefixOf(kind));
	}

	@Transactional
	public ItemResponse create(ItemCreateRequest request, String creId) {
		String itemName = request.getItemName().trim();
		if (!StringUtils.hasText(itemName)) {
			throw new IllegalArgumentException("품목명을 입력하세요.");
		}
		String kind = normalizeKind(request.getKind());

		Item item = new Item();
		item.setItemId(nextItemId(prefixOf(kind)));
		item.setItemName(itemName);
		item.setItemType(typeOf(kind));
		item.setUseYn("Y");
		item.setCreId(StringUtils.hasText(creId) ? creId : "admin");
		item.setCreDt(LocalDateTime.now());
		return ItemResponse.from(itemRepository.save(item));
	}

	@Transactional
	public void delete(String itemId) {
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("품목을 찾을 수 없습니다."));
		if (purchaseOrderItemRepository.existsByItemId(item.getItemId())
				|| purchaseLotRepository.existsByItemId(item.getItemId())
				|| stockRepository.existsByItemId(item.getItemId())
				|| workOrderRepository.existsByItemId(item.getItemId())
				|| bomRepository.existsByItemIdOrMaterialIdOrBomId(item.getItemId(), item.getItemId(), item.getItemId())) {
			throw new IllegalStateException("발주, 재고, 작업지시, BOM에서 사용 중인 품목은 삭제할 수 없습니다.");
		}
		itemRepository.delete(item);
	}

	private String normalizeKind(String kind) {
		if (!StringUtils.hasText(kind)) {
			throw new IllegalArgumentException("구분을 선택하세요.");
		}
		String normalized = kind.trim().toUpperCase();
		if (!KIND_RAW.equals(normalized) && !KIND_PRODUCT.equals(normalized)) {
			throw new IllegalArgumentException("구분을 선택하세요.");
		}
		return normalized;
	}

	private String prefixOf(String kind) {
		return KIND_PRODUCT.equals(normalizeKind(kind)) ? PRODUCT_PREFIX : RAW_MATERIAL_PREFIX;
	}

	private String typeOf(String kind) {
		return KIND_PRODUCT.equals(kind) ? PRODUCT_TYPE : RAW_MATERIAL_TYPE;
	}

	private String nextItemId(String prefix) {
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
