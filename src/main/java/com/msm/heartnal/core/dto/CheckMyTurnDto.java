package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-06-01
 * 각 시그널 현황을 위한 DTO
 */
@Getter
@Setter
public class CheckMyTurnDto {
	private boolean isMyTurn;
	private String category;
	private String myPosition;
	private int tryCount;
}
