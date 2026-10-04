package minimes.production.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "TB_PROD_MATERIAL_INPUT")
public class ProdMaterialInput {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "INPUT_SEQ")
	private Long inputSeq;

	@Column(name = "WORK_ORDER_ID", length = 50)
	private String workOrderId;

	@Column(name = "LOT_ID", length = 50)
	private String lotId;

	@Column(name = "MATERIAL_ID", length = 50)
	private String materialId;

	@Column(name = "MATERIAL_NAME", length = 100)
	private String materialName;

	@Column(name = "QTY")
	private BigDecimal qty;

	@Column(name = "UNIT", length = 20)
	private String unit;

	@Column(name = "MATERIAL_LOT_NO", length = 50)
	private String materialLotNo;
}
