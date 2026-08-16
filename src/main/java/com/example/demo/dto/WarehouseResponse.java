package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.domain.Warehouse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WarehouseResponse {

	private String warehouseId;
	private String warehouseName;
	private String warehouseType;
	private String parentWarehouseId;
	private String creId;
	private LocalDateTime creDt;
	private String modId;
	private LocalDateTime modDt;
	private String useYn;
	private String deptId;

	public static WarehouseResponse from(Warehouse warehouse) {
		return WarehouseResponse.builder()
				.warehouseId(warehouse.getWarehouseId())
				.warehouseName(warehouse.getWarehouseName())
				.warehouseType(warehouse.getWarehouseType())
				.parentWarehouseId(warehouse.getParentWarehouseId())
				.creId(warehouse.getCreId())
				.creDt(warehouse.getCreDt())
				.modId(warehouse.getModId())
				.modDt(warehouse.getModDt())
				.useYn(warehouse.getUseYn())
				.deptId(warehouse.getDeptId())
				.build();
	}
}
