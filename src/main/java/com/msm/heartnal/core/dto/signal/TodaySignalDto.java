package com.msm.heartnal.core.dto.signal;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author 최대희
 * @since 2022-11-18
 */
@Getter
@Setter
public class TodaySignalDto {
	private Long todaySignalSeq;
	private Long senderMemberSeq;
	private Long recipientMemberSeq;
	private String coupleCode;

	// 이 필드는 DB와 통신
	private String questions;
	private String senderAnswers;
	private String recipientAnswers;

	// 이 필드는 Front 와 통신
	private List<String> questionList;
	private List<String> senderAnswerList;
	private List<String> recipientAnswerList;

	private boolean senderComplete;
	private boolean recipientComplete;
	private int finalScore;

	private String regDt;
}
