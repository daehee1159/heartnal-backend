package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.*;
import com.msm.heartnal.core.dto.signal.TodaySignalDto;
import com.msm.heartnal.core.dto.signal.TodaySignalQuestionDto;
import com.msm.heartnal.core.dto.signal.TodaySignalRecordDto;
import com.msm.heartnal.core.enums.NotificationMessage;
import com.msm.heartnal.core.enums.NotificationType;
import com.msm.heartnal.core.mapper.DailySignalMapper;
import com.msm.heartnal.core.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * @author 최대희
 * @since 2022-06-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailySignalServiceImpl implements DailySignalService{
	private final MemberMapper memberMapper;
	private final DailySignalMapper dailySignalMapper;
	private final CloudMessagingService cloudMessagingService;

	@Override
	@Transactional(rollbackFor = {RuntimeException.class, Error.class})
	public boolean sendMessageOfTheDay(MessageOfTheDayDto messageOfTheDayDto) throws IOException {
		try {
			// MessageOfTheDay set & CloudMessage send
			String result = messageOfTheDayDto.getMessage().replaceAll("\\p{Z}", "[]");
			String wordBreak = result.replaceAll("(\r\n|\r|\n|\n\r)", "{}");
			messageOfTheDayDto.setMessage(wordBreak);

			MemberDao myInfo = memberMapper.getMemberInfoBySenderMemberSeq(messageOfTheDayDto.getSenderMemberSeq());
			messageOfTheDayDto.setRecipientMemberSeq(myInfo.getCoupleMemberSeq());

			dailySignalMapper.setMessageOfTheDay(messageOfTheDayDto);

			// notification
			NotificationDto notificationDto = new NotificationDto();
			notificationDto.setUsername(myInfo.getUsername());
			notificationDto.setMemberSeq(myInfo.getCoupleMemberSeq());
			notificationDto.setCoupleMemberSeq(myInfo.getMemberSeq());
			notificationDto.setType(NotificationType.MESSAGE_OF_THE_DAY);
			notificationDto.setMessage(NotificationMessage.MESSAGE_OF_THE_DAY.getText());

			memberMapper.setNotification(notificationDto);

			// 상대방이 보낸 리스트 중 오늘 보낸 메시지가 있는지 확인
			List<MessageOfTheDayDto> senderList = dailySignalMapper.getMessageOfTheDayBySenderSeq(myInfo.getCoupleMemberSeq());
			Collections.reverse(senderList);
			LocalDate today = LocalDate.now();
			boolean termination = false;

			for (MessageOfTheDayDto messageofTheDayDto : senderList) {
				LocalDate compareDate = LocalDate.parse(messageofTheDayDto.getRegDt().substring(0, 10));
				if (today.getYear() == compareDate.getYear() && today.getMonth() == compareDate.getMonth() && today.getDayOfMonth() == compareDate.getDayOfMonth()) {
					termination = true;
					break;
				}
			}

			// tempSignal
			TempSignalDto tempSignalDto = new TempSignalDto();
			tempSignalDto.setCategory("messageOfTheDay");
			tempSignalDto.setSignalSeq(messageOfTheDayDto.getMessageOfTheDaySeq());
			tempSignalDto.setPosition("recipient");
			tempSignalDto.setMemberSeq(messageOfTheDayDto.getRecipientMemberSeq());
			tempSignalDto.setTryCount(1);
			tempSignalDto.setTermination(termination);

			memberMapper.setTempSignal(tempSignalDto);

			// cloudMessage
			CloudMessaging.Android androidConfig = CloudMessaging.Android.builder()
				.priority("high")
				.build();
			CloudMessaging.Apns apnsConfig = CloudMessaging.Apns.builder()
				.priority("5")
				.content_available(true)
				.build();

			CloudMessaging.Data fcmData = CloudMessaging.Data.builder()
				.isSignal("true")
				.position("recipient")
				.category("messageOfTheDay")
				.tryCount("1")
				.messageOfTheDaySeq(messageOfTheDayDto.getMessageOfTheDaySeq().toString())
				.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
				.message(messageOfTheDayDto.getMessage())
				.termination("false")
				.build();
			cloudMessagingService.sendMessageTo(myInfo.getCoupleDeviceToken(), "Heartnal", NotificationMessage.MESSAGE_OF_THE_DAY.getText(), fcmData, androidConfig, apnsConfig);

			return true;
		} catch (Exception e) {
			log.error("MessageOfTheDay Set Error! memberSeq = " + messageOfTheDayDto.getSenderMemberSeq());
			log.error("Error = " + e);

			return false;
		}
	}

	@Override
	public List<MessageOfTheDayDto> getMessageOfTheDay(Long memberSeq, String coupleCode) {
		List<MessageOfTheDayDto> result = dailySignalMapper.getMessageOfTheDay(coupleCode);

		for (MessageOfTheDayDto messageOfTheDayDto : result) {
			String spaceChange = messageOfTheDayDto.getMessage().replaceAll("\\[\\]", " ");
			String wordBreak = spaceChange.replaceAll("\\{\\}", System.lineSeparator());
			messageOfTheDayDto.setMessage(wordBreak);
		}
		Collections.reverse(result);

		return result;
	}

	@Override
	public MessageOfTheDayDto getMessageOfTheDayBySeq(Long messageOfTheDaySeq) {
		MessageOfTheDayDto messageOfTheDayDto = dailySignalMapper.getMessageOfTheDayBySeq(messageOfTheDaySeq);
		String spaceChange = messageOfTheDayDto.getMessage().replaceAll("\\[\\]", " ");
		String wordBreak = spaceChange.replaceAll("\\{\\}", System.lineSeparator());
		messageOfTheDayDto.setMessage(wordBreak);
		return messageOfTheDayDto;
	}

	@Override
	public List<MessageOfTheDayDto> getTodayMessageOfTheDay(String coupleCode) {
		List<MessageOfTheDayDto> messageOfTheDayDtoList = dailySignalMapper.getTodayMessageOfTheDay(coupleCode);
		List<MessageOfTheDayDto> result = new ArrayList<>();

		LocalDate today = LocalDate.now();

		for (MessageOfTheDayDto messageOfTheDayDto : messageOfTheDayDtoList) {
			String spaceChange = messageOfTheDayDto.getMessage().replaceAll("\\[\\]", " ");
			String wordBreak = spaceChange.replaceAll("\\{\\}", System.lineSeparator());
			messageOfTheDayDto.setMessage(wordBreak);
			LocalDate compareDate = LocalDate.parse(messageOfTheDayDto.getRegDt().substring(0, 10));
			if (today.getYear() == compareDate.getYear() && today.getMonth() == compareDate.getMonth() && today.getDayOfMonth() == compareDate.getDayOfMonth()) {
				result.add(messageOfTheDayDto);
			}
		}

		return result;
	}

	@Override
	public boolean deleteMessageOfTheDay(List<MessageOfTheDayDto> messageOfTheDayDtoList) {
		List<Long> messageOfTheDaySeqList = new ArrayList<>();

		for (MessageOfTheDayDto messageOfTheDayDto : messageOfTheDayDtoList) {
			messageOfTheDaySeqList.add(messageOfTheDayDto.getMessageOfTheDaySeq());
		}

		return dailySignalMapper.deleteMessageOfTheDay(messageOfTheDaySeqList);
	}

	@Override
	public TodaySignalDto getTodaySignal(Long todaySignalSeq) {
		return dailySignalMapper.getTodaySignal(todaySignalSeq);
	}

	@Override
	public boolean setTodaySignal(TodaySignalDto todaySignalDto) throws IOException {
		// 오늘 첫 시도이냐 아니냐
		if (todaySignalDto.getTodaySignalSeq() == null) {
			MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(todaySignalDto.getSenderMemberSeq());

			// 첫시도는 set
			todaySignalDto.setRecipientMemberSeq(memberDao.getCoupleMemberSeq());
			todaySignalDto.setSenderComplete(true);
			todaySignalDto.setRecipientComplete(false);

			// List 로 받아온 데이터 String + , 변환 후 저장해야함
			todaySignalDto.setQuestions(String.join(",", todaySignalDto.getQuestionList()));

			todaySignalDto.setSenderAnswers(String.join(",", todaySignalDto.getSenderAnswerList()));

			boolean result = dailySignalMapper.setTodaySignal(todaySignalDto);

			if (result) {

				// set tempSignal
				TempSignalDto tempSignalDto = new TempSignalDto();
				tempSignalDto.setCategory("todaySignal");
				tempSignalDto.setSignalSeq(todaySignalDto.getTodaySignalSeq());
				tempSignalDto.setPosition("recipient");
				tempSignalDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				tempSignalDto.setTryCount(1);
				tempSignalDto.setTermination(false);

				memberMapper.setTempSignal(tempSignalDto);

				// notification
				NotificationDto notificationDto = new NotificationDto();
				notificationDto.setUsername(memberDao.getUsername());
				notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
				notificationDto.setType(NotificationType.TODAY_SIGNAL);
				notificationDto.setMessage(NotificationMessage.TODAY_SIGNAL.getText());

				boolean notificationResult = memberMapper.setNotification(notificationDto);

				// FCM 전송
				String titleMessage = NotificationType.HEARTNAL.toString();
				String bodyMessage = NotificationMessage.TODAY_SIGNAL.getText();

				CloudMessaging.Data fcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("recipient")
					.category("todaySignal")
					.tryCount("1")
					.todaySignalSeq(todaySignalDto.getTodaySignalSeq().toString())
					.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
					.termination("false")
					.build();
				CloudMessaging.Android androidConfig = CloudMessaging.Android.builder()
					.priority("high")
					.build();
				CloudMessaging.Apns apnsConfig = CloudMessaging.Apns.builder()
					.priority("5")
					.content_available(true)
					.build();

				cloudMessagingService.sendMessageTo(memberDao.getCoupleDeviceToken(), titleMessage, bodyMessage, fcmData, androidConfig, apnsConfig);

				return true;
			} else {
				return false;
			}

		} else {
			MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(todaySignalDto.getRecipientMemberSeq());

			// 상대방에 대한 답변일 경우 update
			// recipient 의 데이터만 바꾸면 되기 때문에 해당 필드들만 업데이트 하면 됨

			// List 로 받아온 데이터 String + , 변환 후 저장해야함
			// recipient 일 때 questionList 필요 없을 듯? 어짜피 업데이트기 때문에 건드릴 필요가 없음
//			todaySignalDto.setQuestions(String.join(",", todaySignalDto.getQuestionList()));
			todaySignalDto.setRecipientAnswers(String.join(",", todaySignalDto.getRecipientAnswerList()));

			todaySignalDto.setRecipientComplete(true);

			// 기존 데이터와 비교하여 finalScore set
			TodaySignalDto signalDto = dailySignalMapper.getTodaySignal(todaySignalDto.getTodaySignalSeq());

			List<String> senderAnswerList = new ArrayList<>(Arrays.asList(signalDto.getSenderAnswers().split(",")));

			int finalScore = 0;

			for (int i = 0; i < senderAnswerList.size(); i++) {
				if (Objects.equals(senderAnswerList.get(i), todaySignalDto.getRecipientAnswerList().get(i))) {
					finalScore = finalScore + 10;
				}
			}

			todaySignalDto.setFinalScore(finalScore);

			boolean result =  dailySignalMapper.updateTodaySignal(todaySignalDto);

			if (result) {
//				// set tempSignal
				TempSignalDto tempSignalDto = new TempSignalDto();
				tempSignalDto.setCategory("todaySignal");
				tempSignalDto.setSignalSeq(todaySignalDto.getTodaySignalSeq());
				tempSignalDto.setPosition("sender");
				tempSignalDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				tempSignalDto.setTryCount(1);
				tempSignalDto.setTermination(true);

				memberMapper.setTempSignal(tempSignalDto);

//				// notification
				NotificationDto notificationDto = new NotificationDto();
				notificationDto.setUsername(memberDao.getUsername());
				notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
				notificationDto.setType(NotificationType.TODAY_SIGNAL);
				notificationDto.setMessage(NotificationMessage.TODAY_SIGNAL.getText());

				boolean notificationResult = memberMapper.setNotification(notificationDto);

				// FCM 전송
				String titleMessage = NotificationType.HEARTNAL.toString();
				String bodyMessage = NotificationMessage.RESULT_SIGNAL.getText();

				CloudMessaging.Data senderFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("sender")
					.category("todaySignal")
					.tryCount("1")
					.todaySignalSeq(todaySignalDto.getTodaySignalSeq().toString())
					.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
					.termination("true")
					.build();
				CloudMessaging.Android androidConfig = CloudMessaging.Android.builder()
					.priority("high")
					.build();
				CloudMessaging.Apns apnsConfig = CloudMessaging.Apns.builder()
					.priority("5")
					.content_available(true)
					.build();

				CloudMessaging.Data recipientFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("recipient")
					.category("todaySignal")
					.tryCount("1")
					.todaySignalSeq(todaySignalDto.getTodaySignalSeq().toString())
					.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
					.termination("true")
					.build();

				// sender, recipient 둘 다 보내야 함
				cloudMessagingService.sendMessageTo(memberDao.getMyDeviceToken(), titleMessage, bodyMessage, recipientFcmData, androidConfig, apnsConfig);
				cloudMessagingService.sendMessageTo(memberDao.getCoupleDeviceToken(), titleMessage, bodyMessage, senderFcmData, androidConfig, apnsConfig);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public Map<String, String> getCheckTodaySignal(String coupleCode, Long memberSeq) {
		TodaySignalDto todaySignalDto = dailySignalMapper.getCheckTodaySignal(coupleCode, memberSeq);

		Map<String, String> result = new HashMap<>();

		if (todaySignalDto == null) {
			// 오늘 오늘의 시그널을 이용하지 않은 경우
			result.put("todaySignalSeq", "0");
			result.put("isMyTurn", "Y");
			result.put("isComplete", "N");
		} else if (todaySignalDto.isSenderComplete() && todaySignalDto.isRecipientComplete()) {
			// 오늘 오늘의 시그널을 이용한 경우
			result.put("todaySignalSeq", todaySignalDto.getTodaySignalSeq().toString());
			result.put("isMyTurn", "N");
			result.put("isComplete", "Y");
		} else {
			// 오늘 오늘의 시그널을 이용했지만 둘중에 한명이라도 아직 답을 하지 않은 경우
			result.put("todaySignalSeq", todaySignalDto.getTodaySignalSeq().toString());
			result.put("isMyTurn", (Objects.equals(todaySignalDto.getRecipientMemberSeq(), memberSeq)) ? "Y" : "N");
			result.put("isComplete", "N");
		}

		return result;
	}

	@Override
	public List<TodaySignalDto> getTodaySignalRecord(String coupleCode, Long memberSeq) {
		return dailySignalMapper.getAllTodaySignal(coupleCode);
	}

	@Override
	public List<TodaySignalRecordDto> getTodaySignalRecordDetail(Long todaySignalSeq) {
		List<TodaySignalRecordDto> result = new ArrayList<>();

		TodaySignalDto todaySignalDto = dailySignalMapper.getTodaySignal(todaySignalSeq);

		// question list
		List<String> questionList = new ArrayList<>(Arrays.asList(todaySignalDto.getQuestions().split(",")));
		// sender answer list
		List<String> senderAnswerList = new ArrayList<>(Arrays.asList(todaySignalDto.getSenderAnswers().split(",")));
		// recipient answer list
		List<String> recipientAnswerList = new ArrayList<>(Arrays.asList(todaySignalDto.getRecipientAnswers().split(",")));

		List<Long> tempList = new ArrayList<>();

		for (int i = 0; i < questionList.size(); i++) {
			tempList.clear();
			tempList.add(Long.parseLong(questionList.get(i)));

			List<TodaySignalQuestionDto> questionDto = dailySignalMapper.getQuestionList(tempList);

			TodaySignalRecordDto todaySignalRecordDto = new TodaySignalRecordDto();

			// question 담기
			todaySignalRecordDto.setQuestion(questionDto.get(0).getQuestion());
			// sender answer 담기
			switch (Integer.parseInt(senderAnswerList.get(i))) {
				case 1:
					todaySignalRecordDto.setSenderAnswer(questionDto.get(0).getAnswer1());
					break;
				case 2:
					todaySignalRecordDto.setSenderAnswer(questionDto.get(0).getAnswer2());
					break;
				case 3:
					todaySignalRecordDto.setSenderAnswer(questionDto.get(0).getAnswer3());
					break;
				case 4:
					todaySignalRecordDto.setSenderAnswer(questionDto.get(0).getAnswer4());
					break;
				case 5:
					todaySignalRecordDto.setSenderAnswer(questionDto.get(0).getAnswer5());
					break;
				case 6:
					todaySignalRecordDto.setSenderAnswer(questionDto.get(0).getAnswer6());
					break;
			}
			// recipient answer 담기
			switch (Integer.parseInt(recipientAnswerList.get(i))) {
				case 1:
					todaySignalRecordDto.setRecipientAnswer(questionDto.get(0).getAnswer1());
					break;
				case 2:
					todaySignalRecordDto.setRecipientAnswer(questionDto.get(0).getAnswer2());
					break;
				case 3:
					todaySignalRecordDto.setRecipientAnswer(questionDto.get(0).getAnswer3());
					break;
				case 4:
					todaySignalRecordDto.setRecipientAnswer(questionDto.get(0).getAnswer4());
					break;
				case 5:
					todaySignalRecordDto.setRecipientAnswer(questionDto.get(0).getAnswer5());
					break;
				case 6:
					todaySignalRecordDto.setRecipientAnswer(questionDto.get(0).getAnswer6());
					break;
			}

			todaySignalRecordDto.setTodaySignalSeq(todaySignalSeq);
			todaySignalRecordDto.setTodaySignalQuestionSeq(Long.parseLong(questionList.get(i)));

			result.add(todaySignalRecordDto);
		}

		return result;
	}

	@Override
	public TodaySignalDto getTempTodaySignal(Long todaySignalSeq) {
		return dailySignalMapper.getTodaySignal(todaySignalSeq);
	}

	@Override
	public List<TodaySignalQuestionDto> getQuestionList(String position, Long todaySignalSeq) {
		if (Objects.equals(position, "sender")) {
			List<TodaySignalQuestionDto> list = dailySignalMapper.getQuestions();
			// 각 필드에 하나씩 있던 객관식 답변들을 하나의 List 에 넣어주는 작업
			for (TodaySignalQuestionDto todaySignalQuestionDto : list) {
				List<String> answerList = new ArrayList<>();
				if (todaySignalQuestionDto.getAnswer1() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer1());
				}
				if (todaySignalQuestionDto.getAnswer2() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer2());
				}
				if (todaySignalQuestionDto.getAnswer3() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer3());
				}
				if (todaySignalQuestionDto.getAnswer4() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer4());
				}
				if (todaySignalQuestionDto.getAnswer5() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer5());
				}
				if (todaySignalQuestionDto.getAnswer6() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer6());
				}
				todaySignalQuestionDto.setAnswerList(answerList);
			}
			return list;
		} else {
			TodaySignalDto todaySignalDto = dailySignalMapper.getTodaySignal(todaySignalSeq);
			// String to List<String>
			List<String> questionList = new ArrayList<>(Arrays.asList(todaySignalDto.getQuestions().split(",")));

			List<Long> questionListToLong = new ArrayList<>();
			// List<String> to List<Long>
			List<TodaySignalQuestionDto> result = new ArrayList<>();
			List<TodaySignalQuestionDto> tempList = new ArrayList<>();
			List<Long> tempLongList = new ArrayList<>();

			// String to Long seq 변경
			// Long seq List 생성 후 담기
			for (String s : questionList) {
				questionListToLong.add(Long.parseLong(s));
			}
			// 해당 list 에서 seq 를 하나씩 꺼내서 getQuestionList 함수 호출
			// 해당 리스트를 result 리스트에 하나씩 담기
			for (int i = 0; i < questionListToLong.size(); i++) {
				tempList.clear();
				tempLongList.clear();
				tempLongList.add(questionListToLong.get(i));
				tempList = dailySignalMapper.getQuestionList(tempLongList);
				result.add(tempList.get(0));
			}

			for (TodaySignalQuestionDto todaySignalQuestionDto : result) {
				List<String> answerList = new ArrayList<>();

				if (todaySignalQuestionDto.getAnswer1() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer1());
				}

				if (todaySignalQuestionDto.getAnswer2() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer2());
				}

				if (todaySignalQuestionDto.getAnswer3() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer3());
				}

				if (todaySignalQuestionDto.getAnswer4() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer4());
				}

				if (todaySignalQuestionDto.getAnswer5() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer5());
				}

				if (todaySignalQuestionDto.getAnswer6() != null) {
					answerList.add(todaySignalQuestionDto.getAnswer6());
				}
				todaySignalQuestionDto.setAnswerList(answerList);
			}
			return result;
		}
	}
}

