package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-08-09
 */
@Getter
@Setter
public class CalendarRequestDto {
	private Long calendarSeq;
	private String username;
	private String coupleCode;

//	private String datetime;

	private boolean isPeriod;
	private String startDt;
	private String endDt;

	private String color;

	private String memo;

//	private Map<String, List<String>> calendarList;

}
