package minimes.master.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "TB_WORKCENTER")
public class Workcenter {

	@Id
	@Column(name = "WORKCENTER_ID", length = 50)
	private String workcenterId;

	@Column(name = "WORKCENTER_NAME", length = 100)
	private String workcenterName;

	@Column(name = "USE_YN", length = 1)
	private String useYn;
}
