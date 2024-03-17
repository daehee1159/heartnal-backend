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
public class TodaySignalQuestionDto {
	private Long todaySignalQuestionSeq;

	private String question;
	private String answer1;
	private String answer2;
	private String answer3;
	private String answer4;
	private String answer5;
	private String answer6;
	private List<String> answerList;

	private String modDt;
	private String regDt;
}
