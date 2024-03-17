package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-09-01
 */
@Getter
@Setter
public class MyProfileInfo {
	// 닉네임 업데이트 시 누군지 구별해야 하므로 필요함
	private Long memberSeq;
	private String username;

	private String nickName;
	private String coupleNickName;

	private String myProfileImgAddr;
	private String coupleProfileImgAddr;
	private String mainBannerImgAddr;

	private String coupleRegDt;

	private String myExpression;
	private String coupleExpression;
}
