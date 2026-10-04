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
@Table(name = "TB_EQUIPMENT")
public class Equipment {

	@Id
	@Column(name = "EQUIP_ID", length = 50)
	private String equipId;

	@Column(name = "EQUIP_NAME", length = 100)
	private String equipName;

	@Column(name = "PLANT_ID", length = 50)
	private String plantId;

	@Column(name = "PROCESS_ID", length = 50)
	private String processId;

	@Column(name = "WAREHOUSE_ID", length = 50)
	private String warehouseId;

	@Column(name = "EQUIP_TYPE", length = 50)
	private String equipType;

	@Column(name = "CRE_ID", length = 50)
	private String creId;

	@Column(name = "CRE_DT")
	private LocalDateTime creDt;

	@Column(name = "MOD_ID", length = 50)
	private String modId;

	@Column(name = "MOD_DT")
	private LocalDateTime modDt;
}
