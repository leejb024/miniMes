package minimes.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessSaveRequest {

	@NotBlank(message = "공정코드를 입력하세요.")
	@Size(max = 50, message = "공정코드는 50자 이하로 입력하세요.")
	private String processId;

	@NotBlank(message = "공정명을 입력하세요.")
	@Size(max = 100, message = "공정명은 100자 이하로 입력하세요.")
	private String processName;

	private String useYn;
}
