package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-08-18
 */
@Getter
@Setter
public class CoupleCodeDto {
	private String username;

	private Long memberSeq;
	private Long coupleMemberSeq;
	private String coupleNickName;
	private String coupleCode;

	private String message;

	private String myDeviceToken;
	private String coupleDeviceToken;

	private String coupleRegDt;
}
