package com.example.demo.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TB_USER")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "USER_SEQ")
	private Long userSeq;

	@Column(name = "USER_ID", nullable = false, unique = true, length = 50)
	private String userId;

	@Column(name = "USER_NM", length = 100)
	private String userNm;

	@Column(name = "PASSWORD", nullable = false, length = 255)
	private String password;

	@Builder.Default
	@Column(name = "USE_YN", length = 1, nullable = false)
	private String useYn = "Y";

	@Column(name = "CREATED_AT")
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (useYn == null) {
			useYn = "Y";
		}
	}

	public boolean isActive() {
		return "Y".equalsIgnoreCase(useYn);
	}
}
