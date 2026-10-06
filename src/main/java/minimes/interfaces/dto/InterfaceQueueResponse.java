package minimes.interfaces.dto;

import java.time.LocalDateTime;

import minimes.interfaces.domain.InterfaceQueue;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterfaceQueueResponse {

	private Long ifSeq;
	private String messageId;
	private String targetSystem;
	private String ifType;
	private String status;
	private Integer retryCnt;
	private String lastError;
	private LocalDateTime creDt;
	private LocalDateTime sentDt;
	private String payload;

	public static InterfaceQueueResponse from(InterfaceQueue queue) {
		return InterfaceQueueResponse.builder()
				.ifSeq(queue.getIfSeq())
				.messageId(queue.getMessageId())
				.targetSystem(queue.getTargetSystem())
				.ifType(queue.getIfType())
				.status(queue.getStatus())
				.retryCnt(queue.getRetryCnt())
				.lastError(queue.getLastError())
				.creDt(queue.getCreDt())
				.sentDt(queue.getSentDt())
				.payload(queue.getPayload())
				.build();
	}
}
