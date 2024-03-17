package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-07-09
 */
@Getter
@Setter
public class PlaySignalDto {
	private String username;

	// FCM을 위한 필드
	private String deviceToken;
	private String title;
	private String body;

	private Long playSignalSeq;
	private Long senderMemberSeq;
	private Long recipientMemberSeq;
	private String coupleCode;

	private String category;

	private String senderPrimarySelected;
	private String recipientPrimarySelected;
	private Boolean primaryResult;

	private String senderSecondarySelected;
	private String recipientSecondarySelected;
	private Boolean secondaryResult;

	private String senderTertiarySelected;
	private String recipientTertiarySelected;
	private Boolean tertiaryResult;

	private Boolean finalResult;
	private String finalResultItem;
}
