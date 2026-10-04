package minimes.production.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface WorkOrderView {

	LocalDate getPlanDate();

	String getWorkOrderId();

	String getWorkcenterId();

	String getWorkcenterName();

	String getProcessId();

	String getProcessName();

	String getEquipId();

	String getEquipName();

	String getItemId();

	String getState();

	String getUnit();

	BigDecimal getPlanQty();

	LocalDate getExpiredDate();

	String getItemVersion();

	String getBomVersion();

	String getCloseYn();
}
