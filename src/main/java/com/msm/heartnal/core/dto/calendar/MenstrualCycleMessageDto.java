package com.msm.heartnal.core.dto.calendar;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-08-03
 */
@Getter
@Setter
public class MenstrualCycleMessageDto {
	private Long menstrualCycleMessageSeq;
	private Long memberSeq;
	private Long coupleMemberSeq;
	private String coupleCode;

	private String menstruation3DaysAgoAlarm;
	private String menstruation3DaysAgo;
	private String menstruationDtAlarm;
	private String menstruationDt;
	private String ovulationDtAlarm;
	private String ovulationDt;
	private String fertileWindowStartDtAlarm;
	private String fertileWindowStartDt;
	private String fertileWindowsEndDtAlarm;
	private String fertileWindowsEndDt;

	private String modDt;
	private String regDt;
}
