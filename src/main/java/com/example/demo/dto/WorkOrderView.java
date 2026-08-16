package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface WorkOrderView {

	LocalDate getPlanDate();

	String getWorkOrderId();

	String getWorkcenterId();

	String getWorkcenterName();

	String getItemId();

	String getDeptId();

	String getDeptName();

	String getState();

	String getUnit();

	BigDecimal getPlanQty();

	LocalDate getExpiredDate();

	String getItemVersion();

	String getBomVersion();
}
