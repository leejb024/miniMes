package minimes.master.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Equipment;
import minimes.master.domain.Process;
import minimes.master.domain.Warehouse;
import minimes.master.dto.EquipmentResponse;
import minimes.master.dto.EquipmentSaveRequest;
import minimes.master.repository.EquipmentRepository;
import minimes.master.repository.ProcessRepository;
import minimes.master.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipmentService {

	private final EquipmentRepository equipmentRepository;
	private final ProcessRepository processRepository;
	private final WarehouseRepository warehouseRepository;

	@Transactional(readOnly = true)
	public List<EquipmentResponse> findAll() {
		return equipmentRepository.findAllByOrderByEquipIdAsc().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public EquipmentResponse create(EquipmentSaveRequest request, String creId) {
		String equipId = request.getEquipId().trim();
		if (equipmentRepository.existsById(equipId)) {
			throw new IllegalArgumentException("이미 존재하는 설비 ID입니다.");
		}
		Equipment equipment = new Equipment();
		equipment.setEquipId(equipId);
		apply(equipment, request);
		equipment.setCreId(StringUtils.hasText(creId) ? creId : "admin");
		equipment.setCreDt(LocalDateTime.now());
		return toResponse(equipmentRepository.save(equipment));
	}

	@Transactional
	public EquipmentResponse update(EquipmentSaveRequest request, String modId) {
		String equipId = request.getEquipId().trim();
		Equipment equipment = equipmentRepository.findById(equipId)
				.orElseThrow(() -> new IllegalArgumentException("수정할 설비가 없습니다."));
		apply(equipment, request);
		equipment.setModId(StringUtils.hasText(modId) ? modId : "admin");
		equipment.setModDt(LocalDateTime.now());
		return toResponse(equipment);
	}

	private void apply(Equipment equipment, EquipmentSaveRequest request) {
		Process process = requireProcess(request.getProcessId());
		Warehouse warehouse = resolveWarehouse(request.getWarehouseId());
		equipment.setEquipName(request.getEquipName().trim());
		equipment.setPlantId(request.getPlantId().trim());
		equipment.setProcessId(process.getProcessId());
		equipment.setWarehouseId(warehouse == null ? null : warehouse.getWarehouseId());
		equipment.setEquipType(trimToNull(request.getEquipType()));
	}

	private Process requireProcess(String processId) {
		if (!StringUtils.hasText(processId)) {
			throw new IllegalArgumentException("공정을 선택하세요.");
		}
		return processRepository.findById(processId.trim())
				.orElseThrow(() -> new IllegalArgumentException("공정관리에 없는 공정입니다."));
	}

	private Warehouse resolveWarehouse(String warehouseId) {
		if (!StringUtils.hasText(warehouseId)) {
			return null;
		}
		return warehouseRepository.findById(warehouseId.trim())
				.orElseThrow(() -> new IllegalArgumentException("창고관리에 없는 창고입니다."));
	}

	private EquipmentResponse toResponse(Equipment equipment) {
		String processName = processRepository.findById(equipment.getProcessId())
				.map(Process::getProcessName)
				.orElse("");
		String warehouseName = "";
		if (StringUtils.hasText(equipment.getWarehouseId())) {
			warehouseName = warehouseRepository.findById(equipment.getWarehouseId())
					.map(Warehouse::getWarehouseName)
					.orElse("");
		}
		return EquipmentResponse.builder()
				.equipId(equipment.getEquipId())
				.equipName(equipment.getEquipName())
				.plantId(equipment.getPlantId())
				.processId(equipment.getProcessId())
				.processName(processName)
				.warehouseId(equipment.getWarehouseId())
				.warehouseName(warehouseName)
				.equipType(equipment.getEquipType())
				.build();
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
