package com.msm.heartnal.core.dto;

import com.msm.heartnal.core.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-08-16
 */
@Getter
@Setter
public class NotificationDto {
	private String username;

	private Long memberSeq;
	private Long coupleMemberSeq;
	private NotificationType type;
	private String calendarDt;
	private String message;
	private String regDt;
	private String readDt;
}
