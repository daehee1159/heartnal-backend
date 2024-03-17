package com.msm.heartnal.core.enums;

/**
 * @author 최대희
 * @since 2022-08-03
 */
public enum MenstrualCycleMessage {
	MENSTRUATION_3DAYS_AGO("오늘은 생리 예정 3일전이에요."),
	MENSTRUATION_DAY("오늘은 생리 예정일이에요."),
	OVULATION_DAY("오늘은 배란 예정일이에요."),
	FERTILE_WINDOW_START_DATE("오늘은 가임기 시작일이에요."),
	FERTILE_WINDOW_END_DATE("오늘은 가임기 마지막날이에요.");

	private final String text;

	MenstrualCycleMessage(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}
}
