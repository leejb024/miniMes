package minimes.purchase.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Item;
import minimes.purchase.domain.PurchaseOrder;
import minimes.purchase.domain.PurchaseOrderItem;
import minimes.master.domain.Vendor;
import minimes.purchase.dto.PurchaseOrderItemResponse;
import minimes.purchase.dto.PurchaseOrderRequest;
import minimes.purchase.dto.PurchaseOrderResponse;
import minimes.master.repository.ItemRepository;
import minimes.purchase.repository.PurchaseOrderItemRepository;
import minimes.purchase.repository.PurchaseOrderRepository;
import minimes.master.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

	private static final DateTimeFormatter PO_DATE_PREFIX = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final PurchaseOrderRepository purchaseOrderRepository;
	private final PurchaseOrderItemRepository purchaseOrderItemRepository;
	private final VendorRepository vendorRepository;
	private final ItemRepository itemRepository;

	@Transactional(readOnly = true)
	public List<PurchaseOrderResponse> findAll() {
		List<PurchaseOrder> orders = purchaseOrderRepository.findAllByOrderByPoNoAsc();
		if (orders.isEmpty()) {
			return List.of();
		}

		List<String> poNos = new ArrayList<>();
		for (PurchaseOrder order : orders) {
			poNos.add(order.getPoNo());
		}

		Map<String, List<PurchaseOrderItemResponse>> itemsByPoNo = new HashMap<>();
		List<PurchaseOrderItem> orderItems = purchaseOrderItemRepository.findByPoNoInOrderByPoItemSeqAsc(poNos);
		for (PurchaseOrderItem item : orderItems) {
			List<PurchaseOrderItemResponse> itemResponses = itemsByPoNo.get(item.getPoNo());
			if (itemResponses == null) {
				itemResponses = new ArrayList<>();
				itemsByPoNo.put(item.getPoNo(), itemResponses);
			}
			itemResponses.add(PurchaseOrderItemResponse.from(item));
		}

		List<PurchaseOrderResponse> responses = new ArrayList<>();
		for (PurchaseOrder order : orders) {
			List<PurchaseOrderItemResponse> items = itemsByPoNo.get(order.getPoNo());
			if (items == null) {
				items = List.of();
			}
			responses.add(PurchaseOrderResponse.from(order, items));
		}
		return responses;
	}

	@Transactional
	public PurchaseOrderResponse create(PurchaseOrderRequest request) {
		String poNo = getPoNo(request);
		if (purchaseOrderRepository.existsById(poNo)) {
			throw new IllegalArgumentException("이미 존재하는 발주번호입니다.");
		}

		PurchaseOrder order = new PurchaseOrder();
		order.setPoNo(poNo);
		setParameter(order, request);
		purchaseOrderRepository.save(order);
		saveItems(poNo, request);
		return toResponse(order);
	}

	@Transactional
	public PurchaseOrderResponse update(String poNo, PurchaseOrderRequest request) {
		PurchaseOrder order = purchaseOrderRepository.findById(poNo)
				.orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));
		setParameter(order, request);
		purchaseOrderRepository.save(order);
		updateItems(poNo, request);
		return toResponse(order);
	}

	@Transactional
	public void delete(String poNo) {
		if (!purchaseOrderRepository.existsById(poNo)) {
			throw new IllegalArgumentException("발주를 찾을 수 없습니다.");
		}
		purchaseOrderItemRepository.deleteByPoNo(poNo);
		purchaseOrderRepository.deleteById(poNo);
	}

	private void setParameter(PurchaseOrder order, PurchaseOrderRequest request) {
		order.setPoType(trimToNull(request.getPoType()));
		order.setPoDate(request.getPoDate());
		order.setVendorId(resolveVendor(request.getVendorId()).getVendorId());
		order.setManagerName(trimToNull(request.getManagerName()));
		order.setDueDate(request.getDueDate());
		order.setRemark(trimToNull(request.getRemark()));
	}

	private Vendor resolveVendor(String vendorId) {
		if (!StringUtils.hasText(vendorId)) {
			throw new IllegalArgumentException("구매거래처를 선택하세요.");
		}
		Vendor vendor = vendorRepository.findById(vendorId.trim())
				.orElseThrow(() -> new IllegalArgumentException("거래처관리에 없는 거래처입니다."));
		if (vendor.getUseYn() != null && !"Y".equalsIgnoreCase(vendor.getUseYn())) {
			throw new IllegalArgumentException("사용할 수 없는 거래처입니다.");
		}
		return vendor;
	}

	private void updateItems(String poNo, PurchaseOrderRequest request) {
		purchaseOrderItemRepository.deleteByPoNo(poNo);
		purchaseOrderItemRepository.flush();
		saveItems(poNo, request);
	}

	private void saveItems(String poNo, PurchaseOrderRequest request) {
		if (request.getItems() == null || request.getItems().isEmpty()) {
			throw new IllegalArgumentException("발주 자재를 1건 이상 입력하세요.");
		}

		List<PurchaseOrderItem> items = new ArrayList<>();
		for (PurchaseOrderRequest.PurchaseOrderItemRequest itemRequest : request.getItems()) {
			items.add(toItem(poNo, request, itemRequest));
		}
		purchaseOrderItemRepository.saveAll(items);
	}

	private PurchaseOrderItem toItem(
			String poNo,
			PurchaseOrderRequest request,
			PurchaseOrderRequest.PurchaseOrderItemRequest itemRequest) {
		Item item = resolvePurchaseItem(itemRequest.getItemId());

		LocalDate dueDate = itemRequest.getDueDate() != null
				? itemRequest.getDueDate()
				: (request.getDueDate() != null ? request.getDueDate() : request.getPoDate());

		return PurchaseOrderItem.builder()
				.poNo(poNo)
				.itemId(item.getItemId())
				.itemName(item.getItemName())
				.qty(itemRequest.getQty())
				.unit(StringUtils.hasText(itemRequest.getUnit()) ? itemRequest.getUnit().trim() : "Kg")
				.dueDate(dueDate)
				.build();
	}

	private Item resolvePurchaseItem(String itemId) {
		if (!StringUtils.hasText(itemId)) {
			throw new IllegalArgumentException("자재를 선택하세요.");
		}
		String trimmedId = itemId.trim();
		if (!trimmedId.startsWith("1")) {
			throw new IllegalArgumentException("품번이 1로 시작하는 자재만 발주할 수 있습니다.");
		}
		Item item = itemRepository.findById(trimmedId)
				.orElseThrow(() -> new IllegalArgumentException("품목관리에 없는 자재입니다."));
		if (item.getUseYn() != null && !"Y".equalsIgnoreCase(item.getUseYn())) {
			throw new IllegalArgumentException("사용할 수 없는 품목입니다.");
		}
		return item;
	}

	private String getPoNo(PurchaseOrderRequest request) {
		if (StringUtils.hasText(request.getPoNo())) {
			return request.getPoNo().trim();
		}

		String prefix = request.getPoDate().format(PO_DATE_PREFIX);
		PurchaseOrder lastOrder = purchaseOrderRepository
				.findTopByPoNoStartingWithOrderByPoNoDesc(prefix)
				.orElse(null);

		int nextSeq = 1;
		if (lastOrder != null) {
			String lastPoNo = lastOrder.getPoNo();
			nextSeq = Integer.parseInt(lastPoNo.substring(prefix.length())) + 1;
		}
		return prefix + String.format("%04d", nextSeq);
	}

	private PurchaseOrderResponse toResponse(PurchaseOrder order) {
		List<PurchaseOrderItemResponse> items = new ArrayList<>();
		for (PurchaseOrderItem item : purchaseOrderItemRepository.findByPoNoOrderByPoItemSeqAsc(order.getPoNo())) {
			items.add(PurchaseOrderItemResponse.from(item));
		}
		return PurchaseOrderResponse.from(order, items);
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
