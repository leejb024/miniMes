package minimes.interfaces.domain;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TB_IF_OUTBOX")
public class InterfaceQueue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "IF_SEQ")
	private Long ifSeq;

	@Column(name = "MESSAGE_ID", length = 80, nullable = false, unique = true)
	private String messageId;

	@Column(name = "TARGET_SYSTEM", length = 20, nullable = false)
	private String targetSystem;

	@Column(name = "IF_TYPE", length = 40, nullable = false)
	private String ifType;

	@Column(name = "STATUS", length = 20, nullable = false)
	private String status;

	@Column(name = "PAYLOAD", length = 4000, nullable = false)
	private String payload;

	@Column(name = "RETRY_CNT")
	private Integer retryCnt;

	@Column(name = "LAST_ERROR", length = 1000)
	private String lastError;

	@Column(name = "CRE_DT")
	private LocalDateTime creDt;

	@Column(name = "SENT_DT")
	private LocalDateTime sentDt;

	@PrePersist
	void onCreate() {
		if (creDt == null) {
			creDt = LocalDateTime.now();
		}
		if (retryCnt == null) {
			retryCnt = 0;
		}
		if (status == null) {
			status = "WAIT";
		}
	}
}
