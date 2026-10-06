package minimes.interfaces;

import org.springframework.stereotype.Component;

import minimes.wms.service.WmsInboundReceiveService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InProcessWmsInboundGateway implements WmsInboundGateway {

	private final WmsInboundReceiveService wmsInboundReceiveService;

	@Override
	public void receiveInbound(WmsInboundPayload payload) {
		wmsInboundReceiveService.accept(payload);
	}
}
