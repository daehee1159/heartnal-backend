package com.msm.heartnal.core.enums;

/**
 * @author 최대희
 * @since 2021-12-17
 */
public enum ResultCode {
	/**
	 * Common
	 */
	TRUE("true"),
	FALSE("false"),
	CHOICE("choice"),

	/**
	 * Couple Code
	 */
	SUCCESS_300("등록 가능한 코드에요."),
	ERROR_301("사용할 수 없는 코드에요."),
	ERROR_302("현재 사용중인 코드와 같아요."),
	ERROR_303("존재하지 않는 코드에요."),
	CHOICE_304("해당 코드로 교체할까요?"),

	/**
	 * Error
	 */
	ERROR_999("알 수 없는 오류에요.");

	private final String text;

	ResultCode(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}
}
