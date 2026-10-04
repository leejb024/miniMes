package minimes.production.dto;

import java.math.BigDecimal;

public interface ProdResultPostStatusView {

	String getWorkOrderId();

	Long getPendingCount();

	Long getResultCount();

	BigDecimal getProdQty();

	Long getPackCount();
}
