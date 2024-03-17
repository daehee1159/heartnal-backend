package com.msm.heartnal.core.enums;

/**
 * @author 최대희
 * @since 2021-06-17
 */
public enum MemberType {
	MEMBER("유저"),

	MANAGER("관리자");

	private final String text;

	MemberType(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}
}
