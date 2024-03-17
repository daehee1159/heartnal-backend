package com.msm.heartnal.core.jwt.enums;

/**
 * @author 최대희
 * @since 2021-06-17
 */
public class CommonException extends Exception {

	private static final long serialVersionUID = 1L;

	private final Exception e;
	private final EnumBaseException enumBaseException;
	private final String message;

	public CommonException(Exception e, EnumBaseException enumBaseException) {
		super();
		this.e = e;
		this.enumBaseException = enumBaseException;
		this.message = "비밀번호 오류";
	}

	public CommonException(Exception e, EnumBaseException enumBaseException, String message) {
		super();
		this.e = e;
		this.enumBaseException = enumBaseException;
		this.message = message;
	}

	public Exception getE() {
		return e;
	}

	public EnumBaseException getEnumBaseException() {
		return enumBaseException;
	}

	public String getMessage() {
		return message;
	}

}
