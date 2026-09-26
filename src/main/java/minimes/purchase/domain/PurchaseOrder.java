package minimes.purchase.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TB_PURCHASE_ORDER")
public class PurchaseOrder {

	@Id
	@Column(name = "PO_NO", length = 50)
	private String poNo;

	@Column(name = "PO_TYPE", length = 50)
	private String poType;

	@Column(name = "PO_DATE")
	private LocalDate poDate;

	@Column(name = "VENDOR_NAME", length = 100)
	private String vendorName;

    @Column(name = "VENDOR_ID", length = 100)
    private String vendorId;

	@Column(name = "DEPT_NAME", length = 100)
	private String deptName;

	@Column(name = "MANAGER_NAME", length = 100)
	private String managerName;

	@Column(name = "DUE_DATE")
	private LocalDate dueDate;

	@Column(name = "REMARK", length = 200)
	private String remark;
}
