package minimes.master.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import minimes.master.dto.ProcessResponse;
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
}
