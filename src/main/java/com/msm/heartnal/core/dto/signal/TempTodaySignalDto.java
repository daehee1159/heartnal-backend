package com.msm.heartnal.core.dto.signal;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-11-18
 * 이게 필요 없을지도
 */
@Getter
@Setter
public class TempTodaySignalDto {
	private Long todaySignalTempSeq;
	private Long senderMemberSeq;
	private Long recipientMemberSeq;
	private String coupleCode;

	private String questions;
	private boolean senderComplete;
	private boolean recipientComplete;

	private String regDt;
}
