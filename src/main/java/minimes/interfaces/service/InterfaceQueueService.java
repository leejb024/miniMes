package minimes.interfaces.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import minimes.master.domain.Warehouse;
import minimes.production.domain.ProdResult;
import minimes.production.domain.WorkOrder;
import minimes.interfaces.domain.InterfaceQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterfaceQueueService {

	@Value("${app.wms.fail-on-send:false}")
	private boolean failOnSend;

	public List<InterfaceQueue> queueWmsInbound(WorkOrder workOrder, Warehouse warehouse, List<ProdResult> results) {
		return null;
	}
 
}
