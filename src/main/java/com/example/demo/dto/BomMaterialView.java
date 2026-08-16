package com.example.demo.dto;

import java.math.BigDecimal;

public interface BomMaterialView {

	String getMaterialId();

	String getItemId();

	BigDecimal getQty();

	String getUnit();

	Long getBomSeq();

	String getItemName();
}
