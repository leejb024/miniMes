package com.example.demo.dto;

import com.example.demo.domain.Workcenter;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkcenterResponse {

	private String workcenterId;
	private String workcenterName;
	private String useYn;

	public static WorkcenterResponse from(Workcenter workcenter) {
		return WorkcenterResponse.builder()
				.workcenterId(workcenter.getWorkcenterId())
				.workcenterName(workcenter.getWorkcenterName())
				.useYn(workcenter.getUseYn())
				.build();
	}
}
