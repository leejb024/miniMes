package minimes.master.dto;

import java.time.LocalDateTime;

import minimes.master.domain.Process;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProcessResponse {

	private String processId;
	private String processName;
	private String useYn;
	private String creId;
	private LocalDateTime creDt;

	public static ProcessResponse from(Process process) {
		return ProcessResponse.builder()
				.processId(process.getProcessId())
				.processName(process.getProcessName())
				.useYn(process.getUseYn())
				.creId(process.getCreId())
				.creDt(process.getCreDt())
				.build();
	}
}
