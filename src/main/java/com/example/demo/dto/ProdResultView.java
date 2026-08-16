package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ProdResultView {

	String getWorkOrderId();

	String getPlantId();

	String getLotId();

	BigDecimal getProdQty();

	LocalDateTime getProductionStartTime();

	LocalDateTime getProductionEndTime();

	String getProductionNo();

	String getUnit();

	BigDecimal getWmsProdQty();

	BigDecimal getWmsConfirmQty();
}
