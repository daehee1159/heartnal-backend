package com.msm.heartnal.core.dto.signal;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-12-01
 * 시그널 이력 자세히 보기
 */
@Getter
@Setter
public class TodaySignalRecordDto {
	private Long todaySignalSeq;
	private Long senderMemberSeq;
	private Long recipientMemberSeq;
	private String coupleCode;

	private Long todaySignalQuestionSeq;
	private String question;
	private String senderAnswer;
	private String recipientAnswer;

	private boolean isCorrect;
	private String regDt;
}
