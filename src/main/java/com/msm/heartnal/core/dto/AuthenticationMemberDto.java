package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-07-02
 */
@Getter
@Setter
public class AuthenticationMemberDto {
	private Long memberSeq;
	private String username;
	private String email;
}
