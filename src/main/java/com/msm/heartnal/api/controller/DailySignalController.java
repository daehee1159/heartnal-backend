package com.msm.heartnal.api.controller;

import com.msm.heartnal.api.service.DailySignalService;
import com.msm.heartnal.core.dto.MessageOfTheDayDto;
import com.msm.heartnal.core.dto.signal.TodaySignalDto;
import com.msm.heartnal.core.dto.signal.TodaySignalQuestionDto;
import com.msm.heartnal.core.dto.signal.TodaySignalRecordDto;
import com.msm.heartnal.core.mapper.DailySignalMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 최대희
 * @since 2022-06-19
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/daily/signal", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DailySignalController {
	private final DailySignalService dailySignalService;

	private final DailySignalMapper dailySignalMapper;

	/**
	 * Set MessageOfTheDay
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public boolean setMessageOfTheDay(@RequestBody MessageOfTheDayDto messageOfTheDayDto) throws IOException {
		return dailySignalService.sendMessageOfTheDay(messageOfTheDayDto);
	}

	/**
	 * Get MessageOfTheDay
	 */
	@RequestMapping(value = "/{memberSeq}/{coupleCode}", method = RequestMethod.GET)
	public List<MessageOfTheDayDto> getMessageOfTheDay(@PathVariable Long memberSeq, @PathVariable String coupleCode) {
		return dailySignalService.getMessageOfTheDay(memberSeq, coupleCode);
	}

	/**
	 * Get MessageOfTheDay
	 */
	@RequestMapping(value = "/info/{messageOfTheDaySeq}", method = RequestMethod.GET)
	public MessageOfTheDayDto getMessageOfTheDayBySeq(@PathVariable Long messageOfTheDaySeq) {
		return dailySignalService.getMessageOfTheDayBySeq(messageOfTheDaySeq);
	}

	/**
	 * Get MessageOfTheDay
	 */
	@RequestMapping(value = "/today/{coupleCode}", method = RequestMethod.GET)
	public List<MessageOfTheDayDto> getTodayMessageOfTheDay(@PathVariable String coupleCode) {
		return dailySignalService.getTodayMessageOfTheDay(coupleCode);
	}

	/**
	 * Delete MessageOfTheDay
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public boolean deleteMessageOfTheDay(@RequestBody List<MessageOfTheDayDto> messageOfTheDayDtoList) throws IOException {
		return dailySignalService.deleteMessageOfTheDay(messageOfTheDayDtoList);
	}

	/**
	 * Get TodaySignal
	 */
	@RequestMapping(value = "/today/signal/{todaySignalSeq}", method = RequestMethod.GET)
	public TodaySignalDto getTodaySignal(@PathVariable Long todaySignalSeq) {
		return dailySignalService.getTodaySignal(todaySignalSeq);
	}

//	/**
//	 * TODO Get 시그널 이력을 어떻게 보여주냐에 따라 모든 List 줄지 아니면 여기서 정리해서 넘겨줄지 정해야함
//	 */
//	@RequestMapping(value = "/today/signal/all/{tempTodaySignalSeq}", method = RequestMethod.GET)
//	public TodaySignalDto getTempTodaySignalList(@PathVariable Long tempTodaySignalSeq) {
//		return dailySignalService.getTempTodaySignalList(tempTodaySignalSeq);
//	}

	/**
	 * 오늘의 시그널 오늘 했는지 안했는지
	 */
	@RequestMapping(value = "/check/today/signal/{coupleCode}/{memberSeq}", method = RequestMethod.GET)
	public Map<String, String> checkTodaySignal(@PathVariable String coupleCode, @PathVariable Long memberSeq) {
		return dailySignalService.getCheckTodaySignal(coupleCode, memberSeq);
	}

	/**
	 * 오늘의 시그널 이력
	 */
	@RequestMapping(value = "/today/signal/record/{coupleCode}/{memberSeq}", method = RequestMethod.GET)
	public List<TodaySignalDto> getTodaySignalRecord(@PathVariable String coupleCode, @PathVariable Long memberSeq) {
		return dailySignalService.getTodaySignalRecord(coupleCode, memberSeq);
	}

	/**d
	 * 오늘의 시그널 이력 자세히 보기
	 */
	@RequestMapping(value = "/today/signal/record/{todaySignalSeq}", method = RequestMethod.GET)
	public List<TodaySignalRecordDto> getTodaySignalRecordDetail(@PathVariable Long todaySignalSeq) {
		return dailySignalService.getTodaySignalRecordDetail(todaySignalSeq);
	}

	/**
	 * Set TodaySignal
	 */
	@RequestMapping(value = "/today/signal", method = RequestMethod.POST)
	public boolean setTodaySignal(@RequestBody TodaySignalDto todaySignalDto) throws IOException {
		return dailySignalService.setTodaySignal(todaySignalDto);
	}

	/**
	 * Get All QuestionList
	 */
	@RequestMapping(value = "/today/signal/question/{position}/{todaySignalSeq}", method = RequestMethod.GET)
	public List<TodaySignalQuestionDto> getTodaySignalQuestionList(@PathVariable String position, @PathVariable Long todaySignalSeq) {
		return dailySignalService.getQuestionList(position, todaySignalSeq);
	}

}
