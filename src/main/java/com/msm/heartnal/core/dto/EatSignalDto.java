package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-07-09
 */
@Getter
@Setter
public class EatSignalDto {
	private String username;

	// FCM을 위한 필드
	private String deviceToken;
	private String title;
	private String body;

	private Long eatSignalSeq;
	private Long senderMemberSeq;
	private Long recipientMemberSeq;
	private String coupleCode;

	// 한식,중식 등 카테고리
	private String category;

	// 1차 선택
	private String senderPrimarySelected;
	private String recipientPrimarySelected;
	private Boolean primaryResult;

	// 2차 선택
	private String senderSecondarySelected;
	private String recipientSecondarySelected;
	private Boolean secondaryResult;

	// 3차 선택
	private String senderTertiarySelected;
	private String recipientTertiarySelected;
	private Boolean tertiaryResult;

	// 최종 결과
	private Boolean finalResult;
	private String finalResultItem;
}
