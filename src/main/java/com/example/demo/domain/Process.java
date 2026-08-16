package com.example.demo.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "TB_PROCESS")
public class Process {

	@Id
	@Column(name = "PROCESS_ID", length = 50)
	private String processId;

	@Column(name = "PROCESS_NAME", length = 100)
	private String processName;

	@Column(name = "USE_YN", length = 1)
	private String useYn;

	@Column(name = "CRE_ID", length = 50)
	private String creId;

	@Column(name = "CRE_DT")
	private LocalDateTime creDt;
}
