package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-08-10
 */
@Getter
@Setter
public class CalendarResponseDto {
	//	private String datetime;
	private Long calendarSeq;
	private boolean isPeriod;
	private String startDt;
	private String endDt;

	private String color;

	private String memo;

//	private List<MemoList> memoLists;

//	public static class MemoList {
//		private String memo;
//	}
}
