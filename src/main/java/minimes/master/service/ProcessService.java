package minimes.master.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Process;
import minimes.master.dto.ProcessResponse;
import minimes.master.dto.ProcessSaveRequest;
import minimes.master.repository.ProcessRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessService {

	private final ProcessRepository processRepository;

	@Transactional(readOnly = true)
	public List<ProcessResponse> findAll() {
		return processRepository.findAllByOrderByProcessIdAsc().stream()
				.map(ProcessResponse::from)
				.toList();
	}

	@Transactional
	public ProcessResponse create(ProcessSaveRequest request, String creId) {
		String processId = request.getProcessId().trim();
		if (processRepository.existsById(processId)) {
			throw new IllegalArgumentException("이미 존재하는 공정코드입니다.");
		}
		Process process = new Process();
		process.setProcessId(processId);
		process.setProcessName(request.getProcessName().trim());
		process.setUseYn(normalizeUseYn(request.getUseYn()));
		process.setCreId(StringUtils.hasText(creId) ? creId : "admin");
		process.setCreDt(LocalDateTime.now());
		return ProcessResponse.from(processRepository.save(process));
	}

	@Transactional
	public ProcessResponse update(ProcessSaveRequest request) {
		String processId = request.getProcessId().trim();
		Process process = processRepository.findById(processId)
				.orElseThrow(() -> new IllegalArgumentException("수정할 공정이 없습니다."));
		process.setProcessName(request.getProcessName().trim());
		process.setUseYn(normalizeUseYn(request.getUseYn()));
		return ProcessResponse.from(process);
	}

	private String normalizeUseYn(String useYn) {
		if (!StringUtils.hasText(useYn)) {
			return "Y";
		}
		String normalized = useYn.trim().toUpperCase();
		if (!"Y".equals(normalized) && !"N".equals(normalized)) {
			throw new IllegalArgumentException("사용여부는 Y 또는 N입니다.");
		}
		return normalized;
	}
}
