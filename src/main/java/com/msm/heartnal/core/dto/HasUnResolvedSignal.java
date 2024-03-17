package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-08-11
 */
@Getter
@Setter
public class HasUnResolvedSignal {
	private boolean hasUnResolved;

	private Long senderEatSignalSeq;
	private Long senderPlaySignalSeq;

	private Long recipientEatSignalSeq;
	private Long recipientPlaySignalSeq;
}
