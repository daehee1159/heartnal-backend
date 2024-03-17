package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-10-19
 */
@Getter
@Setter
public class MostMatchedSignalItemDto {
	// parameter
	private Long memberSeq;
	private String coupleCode;
	private String category;
	private String startDt;
	private String endDt;

	// DB data
	private String mostMatchedSignalItem;
	private int mostMatchedSignalItemCount;
}
