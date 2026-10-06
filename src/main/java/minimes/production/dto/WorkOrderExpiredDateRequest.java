package minimes.production.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderExpiredDateRequest {

	@NotBlank(message = "작업지시를 선택하세요.")
	private String workOrderId;

	@NotNull(message = "만기일을 선택하세요.")
	private LocalDate expiredDate;
}
