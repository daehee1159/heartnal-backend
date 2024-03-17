package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-09-14
 */
@Getter
@Setter
public class TotalSignalCountDto {
	private int eatSignalTotalCount;
	private int eatSignalSuccessCount;
	private int playSignalTotalCount;
	private int playSignalSuccessCount;

	private int totalCount;
	private int successCount;
	private int failureCount;
}
