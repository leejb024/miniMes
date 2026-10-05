package minimes.wms.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import minimes.wms.dto.WmsInboundPlanResponse;
import minimes.wms.service.WmsInboundPlanService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wms/inbound-plans")
@RequiredArgsConstructor
public class WmsInboundPlanController {

	private final WmsInboundPlanService wmsInboundPlanService;

	@GetMapping
	public List<WmsInboundPlanResponse> list() {
		return wmsInboundPlanService.findAll();
	}
}
