package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-06-17
 */
@Getter
@Setter
public class MessageOfTheDayDto {
	private Long messageOfTheDaySeq;
	private Long senderMemberSeq;
	private Long recipientMemberSeq;

	private String coupleCode;

	private String message;

	private String regDt;
}
