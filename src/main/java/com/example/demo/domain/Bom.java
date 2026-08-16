package com.example.demo.domain;

import java.math.BigDecimal;
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
@Table(name = "TB_BOM")
public class Bom {

	@Id
	@Column(name = "BOM_SEQ")
	private Long bomSeq;

	@Column(name = "BOM_ID", length = 50)
	private String bomId;

	@Column(name = "BOM_VERSION", length = 50)
	private String bomVersion;

	@Column(name = "PLANT_ID", length = 50)
	private String plantId;

	@Column(name = "ITEM_ID", length = 50)
	private String itemId;

	@Column(name = "MATERIAL_ID", length = 50)
	private String materialId;

	@Column(name = "MATERIAL_TYPE", length = 50)
	private String materialType;

	@Column(name = "PROCESS_ID", length = 50)
	private String processId;

	@Column(name = "QTY")
	private BigDecimal qty;

	@Column(name = "UNIT", length = 20)
	private String unit;

	@Column(name = "USE_YN", length = 1)
	private String useYn;

	@Column(name = "CRE_ID", length = 50)
	private String creId;

	@Column(name = "CRE_DT")
	private LocalDateTime creDt;
}
