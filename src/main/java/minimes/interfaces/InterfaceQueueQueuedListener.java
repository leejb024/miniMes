package minimes.interfaces;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InterfaceQueueQueuedListener {

	private final InterfaceQueueDispatcher interfaceQueueDispatcher;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onQueued(InterfaceQueueEvent event) {
		if (event == null || event.ifSeqs() == null) {
			return;
		}
		for (Long ifSeq : event.ifSeqs()) {
			if (ifSeq == null) {
				continue;
			}
			interfaceQueueDispatcher.dispatchAsync(ifSeq);
		}
	}
}
