package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-05-23
 * 미확인 시그널 DTO
 * senderSelected, recipientSelected 등의 필드를 넣으려고 했으나 이 DTO 는 Flutter 에서 대부분 List 로 반환하기 때문에 List.length > 1 상황이면 해당 필드들을 넣는다고 크게 달라지는게 없음
 */
@Getter
@Setter
public class TempSignalDto {
	private Long tempSignalSeq;
	private String category;
	private Long signalSeq;
	private String position;
	private Long memberSeq;
	// 여기서 tryCount 의 의미는 tryCount 번째 할 차례
	private int tryCount;
	private boolean termination;
}
