package com.msm.heartnal.core.dto;

import com.msm.heartnal.core.enums.MemberStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-08-23
 */
@Getter
@Setter
public class DeleteAccountDto {
	private String username;
	private String reasonMessage;

	private MemberStatus status;
}
