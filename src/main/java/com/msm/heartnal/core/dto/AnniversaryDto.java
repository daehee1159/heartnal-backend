package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-08-25
 */
@Getter
@Setter
public class AnniversaryDto {
	private Long anniversarySeq;
	private String username;
	private String anniversaryDate;
	private String anniversaryTitle;
	// 반복 여부
	private String repeatYN;
	// D-Day 까지 남은 기간
	private String remainingDays;

	// get 으로 조회할 때 같이 담아줄 필드
	private String coupleRegDt;
}
