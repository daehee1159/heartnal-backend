package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dto.MessageOfTheDayDto;
import com.msm.heartnal.core.dto.signal.TodaySignalDto;
import com.msm.heartnal.core.dto.signal.TodaySignalQuestionDto;
import com.msm.heartnal.core.dto.signal.TodaySignalRecordDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 최대희
 * @since 2022-06-17
 * 매일 보내는 시그널류 Service
 */
public interface DailySignalService {
	// MessageOfTheDay Signal
	boolean sendMessageOfTheDay(MessageOfTheDayDto messageOfTheDayDto) throws IOException;
	List<MessageOfTheDayDto> getMessageOfTheDay(Long memberSeq, String coupleCode);

	MessageOfTheDayDto getMessageOfTheDayBySeq(Long messageOfTheDaySeq);

	List<MessageOfTheDayDto> getTodayMessageOfTheDay(String coupleCode);

	boolean deleteMessageOfTheDay(List<MessageOfTheDayDto> messageOfTheDayDtoList);

	/**
	 * TodaySignal
	 */
	TodaySignalDto getTodaySignal(Long todaySignalSeq);
	boolean setTodaySignal(TodaySignalDto todaySignalDto) throws IOException;

	Map<String, String> getCheckTodaySignal(String coupleCode, Long memberSeq);

	// 시그널 이력보기
	List<TodaySignalDto> getTodaySignalRecord(String coupleCode, Long memberSeq);
	// 시그널 이력 자세히 보기
	List<TodaySignalRecordDto> getTodaySignalRecordDetail(Long todaySignalSeq);


	/**
	 * TodaySignalTemp
	 */
	TodaySignalDto getTempTodaySignal(Long tempTodaySignalSeq);

	/**
	 * TodaySignalQuestion
	 */
	List<TodaySignalQuestionDto> getQuestionList(String position, Long todaySignalSeq);
}
