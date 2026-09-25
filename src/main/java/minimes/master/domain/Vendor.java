package minimes.master.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TB_VENDOR")
public class Vendor {

	@Id
	@Column(name = "VENDOR_ID", length = 50)
	private String vendorId;

	@Column(name = "VENDOR_NAME", length = 100)
	private String vendorName;

	@Column(name = "USE_YN", length = 1)
	private String useYn;

	@Column(name = "REG_DT")
	private LocalDateTime regDt;

	@Column(name = "MOD_DT")
	private LocalDateTime modDt;
}
