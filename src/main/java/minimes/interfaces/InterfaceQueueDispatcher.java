package minimes.interfaces;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import minimes.interfaces.service.InterfaceQueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterfaceQueueDispatcher {

	private final InterfaceQueueService interfaceQueueService;

	@Async
	public void dispatchAsync(Long ifSeq) {
		try {
			interfaceQueueService.send(ifSeq);
		} catch (Exception e) {
			log.warn("WMS 비동기 전송 처리에 실패했습니다. ifSeq={}", ifSeq, e);
		}
	}
}
