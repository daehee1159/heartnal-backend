package com.msm.heartnal.core.enums;

/**
 * @author 최대희
 * @since 2021-08-17
 */
public enum NotificationType {
	HEARTNAL("Heartnal"),
	SIGNAL("시그널"),

	EAT_SIGNAL("오늘 뭐먹지?"),

	PLAY_SIGNAL("오늘 뭐하지?"),

	TODAY_SIGNAL("오늘의 시그널"),

	RESULT_EAT_SIGNAL("오늘 뭐먹지 결과"),

	RESULT_PLAY_SIGNAL("오늘 뭐하지 결과"),

	COUPLE_DIARY("커플 다이어리"),

	CALENDAR("캘린더"),

	EXPRESSION("감정표현"),

	MESSAGE_OF_THE_DAY("오늘의 한마디"),

	COMMON("공통");

	private final String text;

	NotificationType(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}
}
