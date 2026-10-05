package minimes.master.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Warehouse;
import minimes.master.dto.WarehouseCreateRequest;
import minimes.master.dto.WarehouseResponse;
import minimes.master.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WarehouseService {

	private final WarehouseRepository warehouseRepository;

	@Transactional(readOnly = true)
	public List<WarehouseResponse> findAll() {
        List<Warehouse> warehouses = warehouseRepository.findAllByOrderByWarehouseIdAsc();
        List<WarehouseResponse> responses = new ArrayList<>();
        for (Warehouse warehouse : warehouses) {
            responses.add(WarehouseResponse.from(warehouse));
        }
        return responses;
	}

	@Transactional
	public WarehouseResponse create(WarehouseCreateRequest request, String creId) {
		String warehouseId = request.getWarehouseId().trim();
		if (warehouseRepository.existsById(warehouseId)) {
			throw new IllegalArgumentException("이미 존재하는 창고ID입니다.");
		}
		Warehouse warehouse = new Warehouse();
		warehouse.setWarehouseId(warehouseId);
		apply(warehouse, request);
		warehouse.setCreId(StringUtils.hasText(creId) ? creId : "admin");
		warehouse.setCreDt(LocalDateTime.now());
		return WarehouseResponse.from(warehouseRepository.save(warehouse));
	}

	@Transactional
	public WarehouseResponse update(WarehouseCreateRequest request, String modId) {
		String warehouseId = request.getWarehouseId().trim();
		Warehouse warehouse = warehouseRepository.findById(warehouseId)
				.orElseThrow(() -> new IllegalArgumentException("수정할 창고가 없습니다."));
		apply(warehouse, request);
		warehouse.setModId(StringUtils.hasText(modId) ? modId : "admin");
		warehouse.setModDt(LocalDateTime.now());
		return WarehouseResponse.from(warehouse);
	}

	private void apply(Warehouse warehouse, WarehouseCreateRequest request) {
		warehouse.setWarehouseName(request.getWarehouseName().trim());
		warehouse.setWarehouseType(trimToNull(request.getWarehouseType()));
		warehouse.setUseYn(normalizeUseYn(request.getUseYn()));
	}

	private String normalizeUseYn(String useYn) {
		if (!StringUtils.hasText(useYn)) {
			return "Y";
		}
		String normalized = useYn.trim().toUpperCase();
		if (!"Y".equals(normalized) && !"N".equals(normalized)) {
			throw new IllegalArgumentException("사용여부는 Y 또는 N입니다.");
		}
		return normalized;
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
