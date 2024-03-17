package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-12-10
 */
@Getter
@Setter
public class MostMatchedItemDto {
	private String signalCategory;
	private String eatSignalFinalResultItem;
	private int eatSignalFinalResultItemCount;

	private String playSignalFinalResultItem;
	private int playSignalFinalResultItemCount;
}
