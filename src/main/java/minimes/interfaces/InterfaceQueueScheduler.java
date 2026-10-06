package minimes.interfaces;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import minimes.interfaces.service.InterfaceQueueService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InterfaceQueueScheduler {

	private final InterfaceQueueService interfaceQueueService;
	private final InterfaceQueueDispatcher interfaceQueueDispatcher;

	@Scheduled(fixedDelayString = "${app.wms.poll-interval-ms:15000}")
	public void dispatchWaiting() {
		for (Long ifSeq : interfaceQueueService.findWaitingSeqs()) {
			interfaceQueueDispatcher.dispatchAsync(ifSeq);
		}
	}
}
