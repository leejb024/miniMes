package com.example.demo.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "TB_WAREHOUSE")
public class Warehouse {

	@Id
	@Column(name = "WAREHOUSE_ID", length = 50)
	private String warehouseId;

	@Column(name = "WAREHOUSE_NAME", length = 100)
	private String warehouseName;

	@Column(name = "WAREHOUSE_TYPE", length = 50)
	private String warehouseType;

	@Column(name = "PARENT_WAREHOUSE_ID", length = 50)
	private String parentWarehouseId;

	@Column(name = "CRE_ID", length = 50)
	private String creId;

	@Column(name = "CRE_DT")
	private LocalDateTime creDt;

	@Column(name = "MOD_ID", length = 50)
	private String modId;

	@Column(name = "MOD_DT")
	private LocalDateTime modDt;

	@Column(name = "USE_YN", length = 1)
	private String useYn;

	@Column(name = "DEPT_ID", length = 50)
	private String deptId;
}
