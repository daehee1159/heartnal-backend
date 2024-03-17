package com.msm.heartnal.core.dto.inquiry;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-09-26
 */
@Getter
@Setter
public class InquiryDto {
	private Long inquirySeq;
	private Long memberSeq;

	private String inquiryTitle;
	private String inquiries;
	private String inquiryDt;

	private String answerContent;
	private String answerDt;
}
