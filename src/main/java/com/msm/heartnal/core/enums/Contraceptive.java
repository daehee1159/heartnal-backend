package com.msm.heartnal.core.enums;

/**
 * @author 최대희
 * @since 2022-08-09
 */
public enum Contraceptive {
	// -6일
	CONTRACEPTIVE_A("21정/휴약기7일"),
	// -6일
	CONTRACEPTIVE_B("21정/위약7정"),
	// -3일
	CONTRACEPTIVE_C("24정/위약4정"),
	// -1일
	CONTRACEPTIVE_D("26정/위약2정"),
	CONTRACEPTIVE_NONE("미설정");

	private final String text;

	Contraceptive(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}
}
