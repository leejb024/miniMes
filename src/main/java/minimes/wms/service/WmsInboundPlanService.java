package minimes.wms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import minimes.wms.dto.WmsInboundPlanResponse;
import minimes.wms.repository.WmsInboundPlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WmsInboundPlanService {

	private final WmsInboundPlanRepository wmsInboundPlanRepository;

	@Transactional(readOnly = true)
	public List<WmsInboundPlanResponse> findAll() {
		return wmsInboundPlanRepository.findAllByOrderByPlanSeqDesc().stream()
				.map(WmsInboundPlanResponse::from)
				.toList();
	}
}
