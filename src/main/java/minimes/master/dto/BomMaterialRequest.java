package minimes.master.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BomMaterialRequest {

	@NotBlank(message = "원료를 선택하세요.")
	private String materialId;

	@NotNull(message = "수량을 입력하세요.")
	@DecimalMin(value = "0.00001", message = "수량은 0보다 커야 합니다.")
	private BigDecimal qty;

	@Size(max = 20, message = "단위는 20자 이하로 입력하세요.")
	private String unit;
}
