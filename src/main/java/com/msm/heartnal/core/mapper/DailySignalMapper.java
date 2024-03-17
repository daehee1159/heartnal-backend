package com.msm.heartnal.core.mapper;

import com.msm.heartnal.core.dto.MemberDto;
import com.msm.heartnal.core.dto.MessageOfTheDayDto;
import com.msm.heartnal.core.dto.signal.TodaySignalDto;
import com.msm.heartnal.core.dto.signal.TodaySignalQuestionDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 최대희
 * @since 2022-06-17
 */
@Mapper
public interface DailySignalMapper {
	// MessageOfTheDay
	boolean setMessageOfTheDay(MessageOfTheDayDto messageOfTheDayDto);

	List<MessageOfTheDayDto> getMessageOfTheDay(String coupleCode);

	MessageOfTheDayDto getMessageOfTheDayBySeq(Long messageOfTheDaySeq);

	List<MessageOfTheDayDto> getMessageOfTheDayBySenderSeq(Long senderMemberSeq);

	List<MessageOfTheDayDto> getTodayMessageOfTheDay(String coupleCode);

	boolean deleteMessageOfTheDay(List<Long> messageOfTheDaySeqList);

	// TodaySignal
	TodaySignalDto getTodaySignal(Long todaySignalSeq);
	TodaySignalDto getCheckTodaySignal(String coupleCode, Long memberSeq);
	List<TodaySignalDto> getAllTodaySignal(String coupleCode);

	boolean setTodaySignal(TodaySignalDto todaySignalDto);
	boolean updateTodaySignal(TodaySignalDto todaySignalDto);
	boolean deleteUnResolvedTodaySignal(MemberDto memberDto);

	List<TodaySignalQuestionDto> getQuestions();
	List<TodaySignalQuestionDto> getQuestionList(List<Long> seqList);
}
