package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dto.calendar.MenstrualCycleCalendarDto;
import com.msm.heartnal.core.dto.calendar.MenstrualCycleDto;
import com.msm.heartnal.core.dto.calendar.MenstrualCycleMessageDto;

import java.text.ParseException;

/**
 * @author 최대희
 * @since 2022-08-03
 */
public interface CalendarService {
	boolean setMenstrualCycle(MenstrualCycleDto menstrualCycleDto);

	MenstrualCycleDto getMenstrualCycle(Long memberSeq, String coupleCode);

	boolean permissionCheck(Long memberSeq, String coupleCode);

	boolean updateMenstrualCycle(MenstrualCycleDto menstrualCycleDto);

	boolean deleteMenstrualCycle(MenstrualCycleDto menstrualCycleDto);

	boolean initMenstrualCycle(Long memberSeq, String coupleCode);

	boolean setMenstrualCycleMessage(MenstrualCycleMessageDto menstrualCycleMessageDto);
	boolean setCoupleMenstrualCycleMessage(MenstrualCycleMessageDto menstrualCycleMessageDto);

	MenstrualCycleMessageDto getMenstrualCycleMessage(Long memberSeq, String coupleCode);
	MenstrualCycleMessageDto getMenstrualCycleCoupleMessage(Long memberSeq, String coupleCode);

	boolean updateMenstrualCycleMessage(MenstrualCycleMessageDto menstrualCycleMessageDto);

	boolean deleteMenstrualCycleMessage(MenstrualCycleMessageDto menstrualCycleMessageDto);

	MenstrualCycleCalendarDto getMenstrualCycleCalendar(Long memberSeq, String coupleCode) throws ParseException;
}
