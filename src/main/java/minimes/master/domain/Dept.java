package minimes.master.domain;

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
@Table(name = "TB_DEPT")
public class Dept {

	@Id
	@Column(name = "DEPT_ID", length = 50)
	private String deptId;

	@Column(name = "DEPT_NAME", length = 100)
	private String deptName;

	@Column(name = "USE_YN", length = 1)
	private String useYn;

	@Column(name = "CRE_ID", length = 50)
	private String creId;

	@Column(name = "CRE_DT")
	private LocalDateTime creDt;

	@Column(name = "MOD_ID", length = 50)
	private String modId;

	@Column(name = "MOD_DT")
	private LocalDateTime modDt;
}
