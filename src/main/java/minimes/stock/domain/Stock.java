package minimes.stock.domain;

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
@Table(name = "TB_STOCK")
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "STOCK_SEQ")
	private Long stockSeq;

	@Column(name = "WAREHOUSE_ID", length = 50)
	private String warehouseId;

	@Column(name = "WAREHOUSE_NAME", length = 100)
	private String warehouseName;

	@Column(name = "ITEM_ID", length = 50)
	private String itemId;

	@Column(name = "ITEM_NAME", length = 100)
	private String itemName;

	@Column(name = "LOT_NO", length = 50)
	private String lotNo;

	@Column(name = "UNIT", length = 20)
	private String unit;

	@Column(name = "QTY")
	private BigDecimal qty;

	@Column(name = "LOCATION_NAME", length = 100)
	private String locationName;
}
