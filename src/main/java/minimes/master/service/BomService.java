package minimes.master.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Bom;
import minimes.master.domain.Item;
import minimes.master.dto.BomItemResponse;
import minimes.master.dto.BomMaterialRequest;
import minimes.master.dto.BomResponse;
import minimes.master.dto.BomSaveRequest;
import minimes.master.repository.BomRepository;
import minimes.master.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BomService {

	private static final String PLANT_ID = "1";
	private static final String USE_Y = "Y";
	private static final String USE_N = "N";

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
				.filter(view -> isRawMaterialCode(view.getMaterialId()))
				.map(BomItemResponse::from)
				.toList();
	}

	@Transactional
	public BomResponse create(BomSaveRequest request, String creId) {
		String itemId = request.getItemId().trim();
		String bomVersion = request.getBomVersion().trim();
		List<Bom> existing = bomRepository.findByItemIdAndBomVersionOrderByBomSeqAsc(itemId, bomVersion);
		if (existing.stream().anyMatch(bom -> USE_Y.equalsIgnoreCase(bom.getUseYn()))) {
			throw new IllegalArgumentException("이미 등록된 BOM입니다. 수정하세요.");
		}
		saveLines(itemId, bomVersion, request.getMaterials(), existing, creId);
		return header(itemId, bomVersion);
	}

	@Transactional
	public BomResponse update(BomSaveRequest request, String creId) {
		String itemId = request.getItemId().trim();
		String bomVersion = request.getBomVersion().trim();
		List<Bom> existing = bomRepository.findByItemIdAndBomVersionOrderByBomSeqAsc(itemId, bomVersion);
		if (existing.stream().noneMatch(bom -> USE_Y.equalsIgnoreCase(bom.getUseYn()))) {
			throw new IllegalArgumentException("수정할 BOM이 없습니다.");
		}
		saveLines(itemId, bomVersion, request.getMaterials(), existing, creId);
		return header(itemId, bomVersion);
	}

	private void saveLines(
			String itemId,
			String bomVersion,
			List<BomMaterialRequest> materials,
			List<Bom> existing,
			String creId) {
		Item product = requireProduct(itemId);
		List<ResolvedMaterial> resolved = resolveMaterials(materials);
		Set<String> requestedIds = new HashSet<>();
		for (ResolvedMaterial material : resolved) {
			requestedIds.add(material.item().getItemId());
		}

		for (Bom bom : existing) {
			if (!requestedIds.contains(bom.getMaterialId()) && USE_Y.equalsIgnoreCase(bom.getUseYn())) {
				bom.setUseYn(USE_N);
			}
		}

		long nextSeq = bomRepository.findMaxBomSeq() + 1;
		String actor = StringUtils.hasText(creId) ? creId : "admin";
		for (ResolvedMaterial material : resolved) {
			Bom row = existing.stream()
					.filter(bom -> material.item().getItemId().equals(bom.getMaterialId()))
					.findFirst()
					.orElse(null);
			if (row == null) {
				row = new Bom();
				row.setBomSeq(nextSeq++);
				row.setBomId(product.getItemId());
				row.setBomVersion(bomVersion);
				row.setPlantId(PLANT_ID);
				row.setItemId(product.getItemId());
				row.setMaterialId(material.item().getItemId());
				row.setMaterialType(product.getItemType());
				row.setUseYn(USE_Y);
				row.setCreId(actor);
				row.setCreDt(LocalDateTime.now());
				applyQty(row, material);
				bomRepository.save(row);
				continue;
			}
			row.setUseYn(USE_Y);
			row.setBomId(product.getItemId());
			row.setItemId(product.getItemId());
			applyQty(row, material);
		}
	}

	private void applyQty(Bom row, ResolvedMaterial material) {
		row.setQty(material.qty());
		row.setUnit(material.unit());
	}

	private BomResponse header(String itemId, String bomVersion) {
		Bom bom = bomRepository.findByItemIdAndBomVersionAndUseYn(itemId, bomVersion, USE_Y).stream()
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("BOM 저장에 실패했습니다."));
		Item item = itemRepository.findById(itemId).orElse(null);
		return BomResponse.fromHeader(bom, item == null ? null : item.getItemName());
	}

	private Item requireProduct(String itemId) {
		if (!isProductCode(itemId)) {
			throw new IllegalArgumentException("품번이 2로 시작하는 제품만 BOM에 등록할 수 있습니다.");
		}
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("제품을 찾을 수 없습니다."));
		if (!"2".equals(item.getItemType())) {
			throw new IllegalArgumentException("ITEM_TYPE이 2인 제품만 BOM에 등록할 수 있습니다.");
		}
		requireUsable(item, "제품");
		return item;
	}

	private List<ResolvedMaterial> resolveMaterials(List<BomMaterialRequest> materials) {
		Set<String> seen = new HashSet<>();
		return materials.stream().map(material -> {
			String materialId = material.getMaterialId().trim();
			if (!seen.add(materialId)) {
				throw new IllegalArgumentException("같은 원료를 중복 등록할 수 없습니다.");
			}
			if (!isRawMaterialCode(materialId)) {
				throw new IllegalArgumentException("품번이 1로 시작하는 원료만 자재로 등록할 수 있습니다.");
			}
			Item item = itemRepository.findById(materialId)
					.orElseThrow(() -> new IllegalArgumentException("원료를 찾을 수 없습니다."));
			requireUsable(item, "원료");
			String unit = StringUtils.hasText(material.getUnit()) ? material.getUnit().trim() : null;
			BigDecimal qty = material.getQty().setScale(5, RoundingMode.HALF_UP);
			return new ResolvedMaterial(item, qty, unit);
		}).toList();
	}

	private void requireUsable(Item item, String label) {
		if (item.getUseYn() != null && !USE_Y.equalsIgnoreCase(item.getUseYn())) {
			throw new IllegalArgumentException("사용 중지된 " + label + "입니다.");
		}
	}

	private record ResolvedMaterial(Item item, BigDecimal qty, String unit) {
	}

	private boolean isProductCode(String itemId) {
		return itemId != null && itemId.startsWith("2");
	}

	private boolean isRawMaterialCode(String materialId) {
		return materialId != null && materialId.startsWith("1");
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
