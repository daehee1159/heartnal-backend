package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-12-23
 */
@Getter
@Setter
public class CoupleDiaryDto {
	private Long diarySeq;
	private Long writerMemberSeq;
	private String coupleCode;

	private String contents;
	private String datetime;
	private String fileName1;
	private String fileName2;
	private String fileName3;

	private boolean likeYN;
	private Long likeMember1;
	private Long likeMember2;
	// DB 컬럼에는 없지만 Service 에서 likeMember 갯수에 따라 정해짐
	private int likeCount;

	private String regDt;

	// 좋아요 클릭으로 인해 필요함
	private Long memberSeq;
}
