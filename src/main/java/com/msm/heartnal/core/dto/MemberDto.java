package com.msm.heartnal.core.dto;

import com.msm.heartnal.core.enums.MemberStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author 최대희
 * @since 2021-06-17
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDto {
	private Long memberSeq;
	private Long coupleMemberSeq;

	private String email;
	private String nickName;
	private String coupleNickName;
	private String gender;

	private String coupleCode;
	private String myDeviceToken;
	private String coupleDeviceToken;

	private String myProfileImgAddr;
	private String coupleProfileImgAddr;
	private String mainBannerImgAddr;

	private String expression;

	private MemberStatus status;
	private String userRole;

	private LocalDateTime regDt;
	private LocalDateTime modDt;
	private String coupleRegDt;
}
