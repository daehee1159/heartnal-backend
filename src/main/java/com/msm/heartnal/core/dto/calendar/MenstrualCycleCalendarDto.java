package com.msm.heartnal.core.dto.calendar;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-08-08
 */
@Getter
@Setter
// 이 DTO 를 리스트로 줄지 아니면 DTO 하나에 모든 데이터를 담을지
public class MenstrualCycleCalendarDto {
	private String startDt;
	private String endDt;

	private String memo;
	private boolean isValid = true;

	// 생리예정
	private String beforeMenstrualCycleStartDt;
	private String beforeMenstrualCycleEndDt;
	private String beforeMenstrualCycleMemo;

	private String menstrualCycleStartDt;
	private String menstrualCycleEndDt;
	private String menstrualCycleMemo;

	private String afterMenstrualCycleStartDt;
	private String afterMenstrualCycleEndDt;
	private String afterMenstrualCycleMemo;

	// 배란일
	private String beforeOvulationDt;
	private String beforeOvulationDtMemo;

	private String ovulationDt;
	private String ovulationDtMemo;

	private String afterOvulationDt;
	private String afterOvulationDtMemo;

	// 가임기
	private String beforeFertileWindowStartDt;
	private String beforeFertileWindowEndDt;
	private String beforeFertileWindowMemo;

	private String fertileWindowStartDt;
	private String fertileWindowEndDt;
	private String fertileWindowMemo;

	private String afterFertileWindowStartDt;
	private String afterFertileWindowEndDt;
	private String afterFertileWindowMemo;
}
