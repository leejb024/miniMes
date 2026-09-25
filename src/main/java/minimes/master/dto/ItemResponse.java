package minimes.master.dto;

import java.time.LocalDateTime;

import minimes.master.domain.Item;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemResponse {

	private String itemId;
	private String itemName;
	private String itemType;
	private String useYn;
	private String creId;
	private LocalDateTime creDt;

	public static ItemResponse from(Item item) {
		return ItemResponse.builder()
				.itemId(item.getItemId())
				.itemName(item.getItemName())
				.itemType(item.getItemType())
				.useYn(item.getUseYn())
				.creId(item.getCreId())
				.creDt(item.getCreDt())
				.build();
	}
}
