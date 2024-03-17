package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-09-07
 */
@Getter
@Setter
public class RecentSignalDto {
	private String category;
	private Long signalSeq;
	private Long senderMemberSeq;
	// 필요 없을지도
	private String username;

	private String nickName;
	private String coupleNickName;
	private Long memberSeq;
	private Long coupleMemberSeq;

	// 결과만 가져오면 되니까 이건 필요없음
	private String senderSelected;
	private String recipientSelected;

	private String finalResult;
	private String finalResultItem;
	private String regDt;
}
