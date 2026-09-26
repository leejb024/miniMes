package minimes.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PurchaseOrderRequest {

	private String poNo;

	@NotBlank(message = "구분을 입력하세요.")
	private String poType;

	@NotNull(message = "발주일을 입력하세요.")
	private LocalDate poDate;

	@NotBlank(message = "구매거래처를 선택하세요.")
	private String vendorId;

	private String managerName;
	private LocalDate dueDate;
	private String remark;

	@NotEmpty(message = "발주 자재를 1건 이상 입력하세요.")
	private List<@Valid PurchaseOrderItemRequest> items = new ArrayList<>();

	@Data
	public static class PurchaseOrderItemRequest {

		@NotBlank(message = "자재를 선택하세요.")
		private String itemId;

		private String itemName;

		@NotNull(message = "발주 수량을 입력하세요.")
		@DecimalMin(value = "0.0001", message = "발주 수량은 0보다 커야 합니다.")
		private BigDecimal qty;

		private String unit;
		private LocalDate dueDate;
	}
}
