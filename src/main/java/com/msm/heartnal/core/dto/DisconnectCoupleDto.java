package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-01-01
 */
@Getter
@Setter
public class DisconnectCoupleDto {
	private Long disconnectSeq;

	private Long memberSeq1;
	private Long memberSeq2;

	private String disconnectDt;
	private String coupleCode;
	private String coupleRegDt;

	private String regDt;
}
