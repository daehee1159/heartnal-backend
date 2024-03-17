package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-08-30
 */
@Getter
@Setter
public class ExpressionDto {
	private String username;
	private String coupleUsername;
	private String myExpression;
	private String coupleExpression;

	private String coupleCode;
}
