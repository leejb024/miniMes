package minimes.master.controller;

import java.util.List;

import minimes.master.dto.ProcessResponse;
import minimes.master.service.ProcessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/processes")
@RequiredArgsConstructor
public class ProcessController {

	private final ProcessService processService;

	@GetMapping
	public List<ProcessResponse> list() {
		return processService.findAll();
	}
}
