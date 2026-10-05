package minimes.master.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import minimes.master.dto.ProcessResponse;
import minimes.master.dto.ProcessSaveRequest;
import minimes.master.service.ProcessService;
import minimes.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProcessResponse create(
			@Valid @RequestBody ProcessSaveRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		String creId = principal == null ? "admin" : principal.getUsername();
		return processService.create(request, creId);
	}

	@PutMapping
	public ProcessResponse update(@Valid @RequestBody ProcessSaveRequest request) {
		return processService.update(request);
	}
}
