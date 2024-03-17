package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-05-26
 */
@Getter
@Setter
public class CoupleUnResolvedSignalDto {
	private boolean hasUnResolved;

	private Long eatSignalSeq;
	private Long playSignalSeq;
	private Long messageOfTheDaySeq;
	private Long todaySignalSeq;
}
