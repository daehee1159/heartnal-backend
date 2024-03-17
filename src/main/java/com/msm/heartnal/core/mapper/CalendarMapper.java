package com.msm.heartnal.core.mapper;

import com.msm.heartnal.core.dto.calendar.MenstrualCycleDto;
import com.msm.heartnal.core.dto.calendar.MenstrualCycleMessageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author 최대희
 * @since 2022-08-04
 */
@Mapper
public interface CalendarMapper {
	boolean setMenstrualCycle(MenstrualCycleDto menstrualCycleDto);

	MenstrualCycleDto getMenstrualCycle(@Param("memberSeq") Long memberSeq, @Param("coupleCode") String coupleCode);

	boolean updateMenstrualCycle(MenstrualCycleDto menstrualCycleDto);
	boolean updateLastMenstrualStartDt(MenstrualCycleDto menstrualCycleDto);

	boolean deleteMenstrualCycle(MenstrualCycleDto menstrualCycleDto);

	boolean setMenstrualCycleMessage(MenstrualCycleMessageDto menstrualCycleMessageDto);

	MenstrualCycleMessageDto getMenstrualCycleMessage(@Param("memberSeq") Long memberSeq, @Param("coupleCode") String coupleCode);

	boolean updateMenstrualCycleMessage(MenstrualCycleMessageDto menstrualCycleMessageDto);

	boolean deleteMenstrualCycleMessage(MenstrualCycleMessageDto menstrualCycleMessageDto);

	boolean deleteMenstrualCycleByCoupleCode(String coupleCode);
	boolean deleteMenstrualCycleMessageByCoupleCode(String coupleCode);

	boolean restoreMenstrualCycleCalendar(String coupleCode);
	boolean restoreMenstrualCycleMessageCalendar(String coupleCode);
}
