package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-08-13
 */
@Getter
@Setter
public class UnResolvedSignalDto {
	private String position;
	private String category;
	private int tryCount;

	private Long eatSignalSeq;
	private Long playSignalSeq;

	private String senderSelected;
	private String recipientSelected;
//	private boolean result;
//	private String resultSelected;
}



