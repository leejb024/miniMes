package minimes.purchase.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TB_PURCHASE_ORDER_ITEM")
public class PurchaseOrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PO_ITEM_SEQ")
	private Long poItemSeq;

	@Column(name = "PO_NO", length = 50)
	private String poNo;

	@Column(name = "ITEM_ID", length = 50)
	private String itemId;

	@Column(name = "ITEM_NAME", length = 100)
	private String itemName;

	@Column(name = "QTY")
	private BigDecimal qty;

	@Column(name = "UNIT", length = 20)
	private String unit;

	@Column(name = "DUE_DATE")
	private LocalDate dueDate;
}
