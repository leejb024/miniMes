package minimes.stock.domain;

import java.time.LocalDate;
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
@Table(name = "TB_STOCK_CLOSE")
public class StockClose {

	public static final String TYPE_MONTH = "MONTH";
	public static final String TYPE_DATE = "DATE";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CLOSE_SEQ")
	private Long closeSeq;

	@Column(name = "CLOSE_TYPE", length = 20, nullable = false)
	private String closeType;

	@Column(name = "CLOSE_MONTH", length = 7)
	private String closeMonth;

	@Column(name = "BASE_DATE", nullable = false)
	private LocalDate baseDate;

	@Column(name = "REMARK", length = 200)
	private String remark;

	@Column(name = "CRE_ID", length = 50)
	private String creId;

	@Column(name = "CRE_DT")
	private LocalDateTime creDt;

	@PrePersist
	void onCreate() {
		if (creDt == null) {
			creDt = LocalDateTime.now();
		}
	}
}
