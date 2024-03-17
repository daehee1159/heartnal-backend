package com.msm.heartnal.core.enums;

/**
 * @author 최대희
 * @since 2021-08-17
 */
public enum NotificationMessage {
	REGISTRATION("하트널 가입을 축하드립니다!"),

	SIGNAL("상대방으로부터 시그널을 받았어요."),

	EAT_SIGNAL("상대방으로부터 오늘 뭐먹지? 시그널을 받았어요."),

	PLAY_SIGNAL("상대방으로부터 오늘 뭐하지? 시그널을 받았어요."),

	TODAY_SIGNAL("오늘의 시그널을 받았어요!"),

	RESULT_SIGNAL("시그널 결과가 도착했어요. 확인해보세요!"),

	COUPLE_DIARY("커플 다이어리가 등록되었어요."),

	EXPRESSION("상대방이 감정표현을 변경했어요. 확인해보세요!"),

	MESSAGE_OF_THE_DAY("상대방으로부터 오늘의 한마디를 받았어요."),

	CALENDAR("커플 캘린더 일정이 등록되었어요.");

	private final String text;

	NotificationMessage(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}
}
