package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.domain.Dept;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeptResponse {

	private String deptId;
	private String deptName;
	private String useYn;
	private String creId;
	private LocalDateTime creDt;
	private String modId;
	private LocalDateTime modDt;

	public static DeptResponse from(Dept dept) {
		return DeptResponse.builder()
				.deptId(dept.getDeptId())
				.deptName(dept.getDeptName())
				.useYn(dept.getUseYn())
				.creId(dept.getCreId())
				.creDt(dept.getCreDt())
				.modId(dept.getModId())
				.modDt(dept.getModDt())
				.build();
	}
}
