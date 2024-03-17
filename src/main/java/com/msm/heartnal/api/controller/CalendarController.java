package com.msm.heartnal.api.controller;

import com.msm.heartnal.api.service.CalendarService;
import com.msm.heartnal.core.dto.calendar.MenstrualCycleCalendarDto;
import com.msm.heartnal.core.dto.calendar.MenstrualCycleDto;
import com.msm.heartnal.core.dto.calendar.MenstrualCycleMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

/**
 * @author 최대희
 * @since 2022-08-03
 * 생리주기 캘린더 및 생리주기 캘린더 알람 메시지 + 추후 기존 캘린더 로직도 이쪽으로 옮겨서 리팩토링 해야함
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/calendar", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CalendarController {
	private final CalendarService calendarService;

	/**
	 * Set MenstrualCycle
	 */
	@RequestMapping(value = "/menstrual", method = RequestMethod.POST)
	public boolean setMenstrualCycle(@RequestBody MenstrualCycleDto menstrualCycleDto) {
		return calendarService.setMenstrualCycle(menstrualCycleDto);
	}

	/**
	 * Get MenstrualCycle
	 */
	@RequestMapping(value = "/menstrual/{memberSeq}/{coupleCode}", method = RequestMethod.GET)
	public MenstrualCycleDto getMenstrualCycle(@PathVariable Long memberSeq, @PathVariable String coupleCode) {
		return calendarService.getMenstrualCycle(memberSeq, coupleCode);
	}

	/**
	 * Check Authority
	 */
	@RequestMapping(value = "/menstrual/permission/{memberSeq}/{coupleCode}", method = RequestMethod.GET)
	public boolean permissionCheck(@PathVariable Long memberSeq, @PathVariable String coupleCode) {
		return calendarService.permissionCheck(memberSeq, coupleCode);
	}

	/**
	 * Update MenstrualCycle
	 */
	@RequestMapping(value = "/menstrual/update", method = RequestMethod.POST)
	public boolean updateMenstrualCycle(@RequestBody MenstrualCycleDto menstrualCycleDto) {
		return calendarService.updateMenstrualCycle(menstrualCycleDto);
	}

	/**
	 * Delete MenstrualCycle
	 */
	@RequestMapping(value = "/menstrual/delete", method = RequestMethod.POST)
	public boolean deleteMenstrualCycle(@RequestBody MenstrualCycleDto menstrualCycleDto) {
		return calendarService.deleteMenstrualCycle(menstrualCycleDto);
	}

	/**
	 * Init MenstrualCycle
	 */
	@RequestMapping(value = "/menstrual/init/{memberSeq}/{coupleCode}", method = RequestMethod.GET)
	public boolean initMenstrualCycle(@PathVariable Long memberSeq, @PathVariable String coupleCode) {
		return calendarService.initMenstrualCycle(memberSeq, coupleCode);
	}

	/**
	 * Set MenstrualCycleMessage
	 */
	@RequestMapping(value = "/menstrual/message", method = RequestMethod.POST)
	public boolean setMenstrualCycleMessage(@RequestBody MenstrualCycleMessageDto menstrualCycleMessageDto) {
		return calendarService.setMenstrualCycleMessage(menstrualCycleMessageDto);
	}

	/**
	 * Set CoupleMenstrualCycleMessage
	 */
	@RequestMapping(value = "/menstrual/couple/message", method = RequestMethod.POST)
	public boolean setCoupleMenstrualCycleMessage(@RequestBody MenstrualCycleMessageDto menstrualCycleMessageDto) {
		return calendarService.setCoupleMenstrualCycleMessage(menstrualCycleMessageDto);
	}

	/**
	 * Get MenstrualCycleMessage
	 */
	@RequestMapping(value = "/menstrual/message/{memberSeq}/{coupleCode}", method = RequestMethod.GET)
	public MenstrualCycleMessageDto getMenstrualCycleMessage(@PathVariable Long memberSeq, @PathVariable String coupleCode) {
		return calendarService.getMenstrualCycleMessage(memberSeq, coupleCode);
	}

	/**
	 * Get MenstrualCycleCoupleMessage
	 */
	@RequestMapping(value = "/menstrual/couple/message/{memberSeq}/{coupleCode}", method = RequestMethod.GET)
	public MenstrualCycleMessageDto getMenstrualCycleCoupleMessage(@PathVariable Long memberSeq, @PathVariable String coupleCode) {
		return calendarService.getMenstrualCycleCoupleMessage(memberSeq, coupleCode);
	}

	/**
	 * Update MenstrualCycleMessage
	 */
	@RequestMapping(value = "/menstrual/message/update", method = RequestMethod.POST)
	public boolean updateMenstrualCycleMessage(@RequestBody MenstrualCycleMessageDto menstrualCycleMessageDto) {
		return calendarService.updateMenstrualCycleMessage(menstrualCycleMessageDto);
	}

	/**
	 * Delete MenstrualCycleMessage
	 */
	@RequestMapping(value = "/menstrual/message/delete", method = RequestMethod.POST)
	public boolean deleteMenstrualCycleMessage(@RequestBody MenstrualCycleMessageDto menstrualCycleMessageDto) {
		return calendarService.deleteMenstrualCycleMessage(menstrualCycleMessageDto);
	}

	/**
	 * Get MenstrualCycle
	 */
	@RequestMapping(value = "/menstrual/calendar/{memberSeq}/{coupleCode}", method = RequestMethod.GET)
	public MenstrualCycleCalendarDto getMenstrualCycleCalendar(@PathVariable Long memberSeq, @PathVariable String coupleCode) throws ParseException {
		return calendarService.getMenstrualCycleCalendar(memberSeq, coupleCode);
	}

}
