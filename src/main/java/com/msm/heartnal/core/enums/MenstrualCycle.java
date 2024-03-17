package com.msm.heartnal.core.enums;

/**
 * @author 최대희
 * @since 2022-08-14
 */
public enum MenstrualCycle {
	MENSTRUAL_CYCLE("생리 예정"),
	OVULATION_DAY("배란일"),
	FERTILE_WINDOW_DATE("가임기");

	private final String text;

	MenstrualCycle(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}
}
