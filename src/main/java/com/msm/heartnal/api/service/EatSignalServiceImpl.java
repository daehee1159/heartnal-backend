package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.*;
import com.msm.heartnal.core.enums.NotificationMessage;
import com.msm.heartnal.core.enums.NotificationType;
import com.msm.heartnal.core.jwt.enums.CommonException;
import com.msm.heartnal.core.mapper.EatSignalMapper;
import com.msm.heartnal.core.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author 최대희
 * @since 2021-07-10
 */
@Service
@RequiredArgsConstructor
public class EatSignalServiceImpl implements EatSignalService {
	private final EatSignalMapper eatSignalMapper;

	private final MemberMapper memberMapper;

	private final CloudMessagingService cloudMessagingService;

	// EatSignal Primary
	@Override
	public int senderEatSignal(EatSignalDto eatSignalDto) throws CommonException, Exception {
		CloudMessaging.Android androidConfig = CloudMessaging.Android.builder()
			.priority("high")
			.build();
		CloudMessaging.Apns apnsConfig = CloudMessaging.Apns.builder()
			.priority("5")
			.content_available(true)
			.build();
		// 첫번째 시도일 경우 insert
		if (eatSignalDto.getEatSignalSeq() == null) {
			MemberDao memberDao = memberMapper.getMemberInfoByUsername(eatSignalDto.getUsername());
			eatSignalDto.setDeviceToken(memberDao.getCoupleDeviceToken());
			eatSignalDto.setTitle(NotificationType.EAT_SIGNAL.getText());
			eatSignalDto.setBody(NotificationMessage.SIGNAL.getText());

			eatSignalDto.setSenderMemberSeq(memberDao.getMemberSeq());
			eatSignalDto.setRecipientMemberSeq(memberDao.getCoupleMemberSeq());
			eatSignalDto.setCoupleCode(memberDao.getCoupleCode());

			// 제너레이트 키 생성
			int result = eatSignalMapper.senderEatSignal(eatSignalDto);

			// 키 확보
			// recipient 에게 eatSignalSeq 전달
			// fcm 데이터에 포함해서 전달 후 flutter 에서 받아서 처리
			if (result == 1) {
				TempSignalDto tempSignalDto = new TempSignalDto();
				tempSignalDto.setCategory("eatSignal");
				tempSignalDto.setSignalSeq(eatSignalDto.getEatSignalSeq());
				tempSignalDto.setPosition("recipient");
				tempSignalDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				tempSignalDto.setTryCount(1);
				tempSignalDto.setTermination(false);

				memberMapper.setTempSignal(tempSignalDto);

				CloudMessaging.Data fcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("recipient")
					.category("eatSignal")
					.tryCount("1")
					.eatSignalSeq(eatSignalDto.getEatSignalSeq().toString())
					.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
					.senderSelected(eatSignalDto.getSenderPrimarySelected())
					.termination("false")
					.build();

				cloudMessagingService.sendMessageTo(eatSignalDto.getDeviceToken(), eatSignalDto.getTitle(), eatSignalDto.getBody(), fcmData, androidConfig, apnsConfig);
				NotificationDto notificationDto = new NotificationDto();
				notificationDto.setUsername(eatSignalDto.getUsername());
				notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
				notificationDto.setType(NotificationType.SIGNAL);
				notificationDto.setMessage(NotificationMessage.EAT_SIGNAL.getText());

				memberMapper.setNotification(notificationDto);

				return result;
			} else {
				throw new Exception();
			}

		} else {
			// 두번째 이상 시도일 경우 update
			EatSignalDto signalInfo = eatSignalMapper.getEatSignalBySignalSeq(eatSignalDto.getEatSignalSeq());
			MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(signalInfo.getSenderMemberSeq());

			// 두번째 시도
			if (signalInfo.getPrimaryResult() != null && signalInfo.getSecondaryResult() == null && signalInfo.getFinalResult() == null) {
				signalInfo.setSenderSecondarySelected(eatSignalDto.getSenderSecondarySelected());

				String title = NotificationType.EAT_SIGNAL.getText();
				String body = NotificationMessage.SIGNAL.getText();

				int result = eatSignalMapper.senderEatSignalUpdate(signalInfo);
				if (result == 1) {
					TempSignalDto tempSignalDto = new TempSignalDto();
					tempSignalDto.setCategory("eatSignal");
					tempSignalDto.setSignalSeq(eatSignalDto.getEatSignalSeq());
					tempSignalDto.setPosition("recipient");
					tempSignalDto.setMemberSeq(memberDao.getCoupleMemberSeq());
					tempSignalDto.setTryCount(2);
					tempSignalDto.setTermination(false);

					memberMapper.setTempSignal(tempSignalDto);

					CloudMessaging.Data fcmData = CloudMessaging.Data.builder()
						.isSignal("true")
						.position("recipient")
						.category("eatSignal")
						.tryCount("2")
						.eatSignalSeq(eatSignalDto.getEatSignalSeq().toString())
						.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
						.senderSelected(signalInfo.getSenderPrimarySelected())
						.recipientSelected(signalInfo.getRecipientPrimarySelected())
						.termination("false")
						.build();

					cloudMessagingService.sendMessageTo(memberDao.getCoupleDeviceToken(), title, body, fcmData, androidConfig, apnsConfig);

					NotificationDto notificationDto = new NotificationDto();
					notificationDto.setUsername(eatSignalDto.getUsername());
					notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
					notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
					notificationDto.setType(NotificationType.SIGNAL);
					notificationDto.setMessage(NotificationMessage.EAT_SIGNAL.getText());

					memberMapper.setNotification(notificationDto);

					return result;
				} else {
					throw new Exception();
				}

			}
			// 세번째 시도
			else if (signalInfo.getPrimaryResult() != null && signalInfo.getSecondaryResult() != null && signalInfo.getFinalResult() == null) {
				signalInfo.setSenderTertiarySelected(eatSignalDto.getSenderTertiarySelected());

				String title = NotificationType.EAT_SIGNAL.getText();
				String body = NotificationMessage.SIGNAL.getText();

				int result = eatSignalMapper.senderEatSignalUpdate(signalInfo);
				if (result == 1) {
					TempSignalDto tempSignalDto = new TempSignalDto();
					tempSignalDto.setCategory("eatSignal");
					tempSignalDto.setSignalSeq(eatSignalDto.getEatSignalSeq());
					tempSignalDto.setPosition("recipient");
					tempSignalDto.setMemberSeq(memberDao.getCoupleMemberSeq());
					tempSignalDto.setTryCount(3);
					tempSignalDto.setTermination(false);

					memberMapper.setTempSignal(tempSignalDto);

					CloudMessaging.Data fcmData = CloudMessaging.Data.builder()
						.isSignal("true")
						.position("recipient")
						.category("eatSignal")
						.tryCount("3")
						.eatSignalSeq(eatSignalDto.getEatSignalSeq().toString())
						.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
						.senderSelected(signalInfo.getSenderSecondarySelected())
						.recipientSelected(signalInfo.getRecipientSecondarySelected())
						.termination("false")
						.build();

					cloudMessagingService.sendMessageTo(memberDao.getCoupleDeviceToken(), title, body, fcmData, androidConfig, apnsConfig);

					NotificationDto notificationDto = new NotificationDto();
					notificationDto.setUsername(eatSignalDto.getUsername());
					notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
					notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
					notificationDto.setType(NotificationType.SIGNAL);
					notificationDto.setMessage(NotificationMessage.EAT_SIGNAL.getText());

					memberMapper.setNotification(notificationDto);

					return result;
				} else {
					throw new Exception();
				}
			} else {
				//TODO
				throw new Exception();
			}
		}
	}

	@Override
	public int recipientEatSignal(EatSignalDto eatSignalDto) throws CommonException, Exception {
		EatSignalDto signalInfo = eatSignalMapper.getEatSignalBySignalSeq(eatSignalDto.getEatSignalSeq());
		signalInfo.setEatSignalSeq(eatSignalDto.getEatSignalSeq());

		MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(signalInfo.getSenderMemberSeq());

		String senderDevice = memberDao.getMyDeviceToken();
		String fcmTitle = NotificationType.RESULT_EAT_SIGNAL.getText();
		String fcmBody = NotificationMessage.RESULT_SIGNAL.getText();

		String recipientDevice = memberDao.getCoupleDeviceToken();

		String resultSelected;

		CloudMessaging.Android androidConfig = CloudMessaging.Android.builder()
			.priority("high")
			.build();
		CloudMessaging.Apns apnsConfig = CloudMessaging.Apns.builder()
			.priority("5")
			.content_available(true)
			.build();

		// 첫번째 시도
		if (signalInfo.getRecipientPrimarySelected() == null && signalInfo.getSenderSecondarySelected() == null && signalInfo.getSenderTertiarySelected() == null) {
			signalInfo.setRecipientPrimarySelected(eatSignalDto.getRecipientPrimarySelected());

			if (signalInfo.getSenderPrimarySelected().equals(eatSignalDto.getRecipientPrimarySelected())) {
				signalInfo.setPrimaryResult(true);
				signalInfo.setFinalResult(true);

				signalInfo.setFinalResultItem(eatSignalDto.getRecipientPrimarySelected());

				resultSelected = signalInfo.getFinalResultItem();

			} else {
				signalInfo.setPrimaryResult(false);
				resultSelected = "";
			}

			int result = eatSignalMapper.recipientEatSignal(signalInfo);

			if (result == 1) {
				TempSignalDto tempSignalDto = new TempSignalDto();
				tempSignalDto.setCategory("eatSignal");
				tempSignalDto.setSignalSeq(eatSignalDto.getEatSignalSeq());
				tempSignalDto.setPosition("sender");
				tempSignalDto.setMemberSeq(signalInfo.getSenderMemberSeq());
				tempSignalDto.setTryCount(1);
				tempSignalDto.setTermination(true);

				memberMapper.setTempSignal(tempSignalDto);

				CloudMessaging.Data senderFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("sender")
					.category("eatSignal")
					.tryCount("1")
					.eatSignalSeq(eatSignalDto.getEatSignalSeq().toString())
					.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
					.senderSelected(signalInfo.getSenderPrimarySelected())
					.recipientSelected(eatSignalDto.getRecipientPrimarySelected())
					.termination("true")
					.result(signalInfo.getPrimaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				CloudMessaging.Data recipientFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("recipient")
					.category("eatSignal")
					.tryCount("1")
					.eatSignalSeq(eatSignalDto.getEatSignalSeq().toString())
					.tempSignalSeq("0")
					.senderSelected(signalInfo.getSenderPrimarySelected())
					.recipientSelected(eatSignalDto.getRecipientPrimarySelected())
					.termination("true")
					.result(signalInfo.getPrimaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				cloudMessagingService.sendMessageTo(senderDevice, fcmTitle, fcmBody, senderFcmData, androidConfig, apnsConfig);
				cloudMessagingService.sendMessageTo(recipientDevice, fcmTitle, fcmBody, recipientFcmData, androidConfig, apnsConfig);

				NotificationDto notificationDto = new NotificationDto();
				notificationDto.setUsername(eatSignalDto.getUsername());
				notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
				notificationDto.setType(NotificationType.SIGNAL);
				notificationDto.setMessage(NotificationMessage.EAT_SIGNAL.getText());

				memberMapper.setNotification(notificationDto);

				return result;
			} else {
				throw new Exception();
			}
		}
		// 2차 시도
		else if (signalInfo.getRecipientPrimarySelected() != null && signalInfo.getRecipientSecondarySelected() == null) {
			signalInfo.setRecipientSecondarySelected(eatSignalDto.getRecipientSecondarySelected());
			// 2차 성공
			if (signalInfo.getSenderSecondarySelected().equals(eatSignalDto.getRecipientSecondarySelected())) {
				signalInfo.setSecondaryResult(true);
				signalInfo.setFinalResult(true);

				signalInfo.setFinalResultItem(eatSignalDto.getRecipientSecondarySelected());

				resultSelected = signalInfo.getFinalResultItem();
			} else {
				// 2차 실패
				signalInfo.setSecondaryResult(false);
				resultSelected = "";
			}

			int result = eatSignalMapper.recipientEatSignal(signalInfo);
			if (result == 1) {
				TempSignalDto tempSignalDto = new TempSignalDto();
				tempSignalDto.setCategory("eatSignal");
				tempSignalDto.setSignalSeq(eatSignalDto.getEatSignalSeq());
				tempSignalDto.setPosition("sender");
				tempSignalDto.setMemberSeq(signalInfo.getSenderMemberSeq());
				tempSignalDto.setTryCount(2);
				tempSignalDto.setTermination(true);

				memberMapper.setTempSignal(tempSignalDto);

				CloudMessaging.Data senderFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("sender")
					.category("eatSignal")
					.tryCount("2")
					.eatSignalSeq(eatSignalDto.getEatSignalSeq().toString())
					.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
					.senderSelected(signalInfo.getSenderSecondarySelected())
					.recipientSelected(eatSignalDto.getRecipientSecondarySelected())
					.termination("true")
					.result(signalInfo.getSecondaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				CloudMessaging.Data recipientFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("recipient")
					.category("eatSignal")
					.tryCount("2")
					.eatSignalSeq(eatSignalDto.getEatSignalSeq().toString())
					.tempSignalSeq("0")
					.senderSelected(signalInfo.getSenderSecondarySelected())
					.recipientSelected(eatSignalDto.getRecipientSecondarySelected())
					.termination("true")
					.result(signalInfo.getSecondaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				cloudMessagingService.sendMessageTo(senderDevice, fcmTitle, fcmBody, senderFcmData, androidConfig, apnsConfig);
				cloudMessagingService.sendMessageTo(recipientDevice, fcmTitle, fcmBody, recipientFcmData, androidConfig, apnsConfig);

				NotificationDto notificationDto = new NotificationDto();
				notificationDto.setUsername(eatSignalDto.getUsername());
				notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
				notificationDto.setType(NotificationType.SIGNAL);
				notificationDto.setMessage(NotificationMessage.EAT_SIGNAL.getText());

				memberMapper.setNotification(notificationDto);

				return result;
			} else {
				throw new Exception();
			}
		}
		// 3차 시도
		else if (signalInfo.getRecipientPrimarySelected() != null && signalInfo.getRecipientSecondarySelected() != null && signalInfo.getRecipientTertiarySelected() == null) {
			signalInfo.setRecipientTertiarySelected(eatSignalDto.getRecipientTertiarySelected());
			// 3차 성공
			if (signalInfo.getSenderTertiarySelected().equals(eatSignalDto.getRecipientTertiarySelected())) {
				signalInfo.setTertiaryResult(true);
				signalInfo.setFinalResult(true);

				signalInfo.setFinalResultItem(eatSignalDto.getRecipientTertiarySelected());

				resultSelected = signalInfo.getFinalResultItem();
			} else {
				// 3차 실패
				signalInfo.setTertiaryResult(false);
				signalInfo.setFinalResult(false);
				resultSelected = "";
			}

			int result = eatSignalMapper.recipientEatSignal(signalInfo);
			if (result == 1) {
				TempSignalDto tempSignalDto = new TempSignalDto();
				tempSignalDto.setCategory("eatSignal");
				tempSignalDto.setSignalSeq(eatSignalDto.getEatSignalSeq());
				tempSignalDto.setPosition("sender");
				tempSignalDto.setMemberSeq(signalInfo.getSenderMemberSeq());
				tempSignalDto.setTryCount(3);
				tempSignalDto.setTermination(true);

				memberMapper.setTempSignal(tempSignalDto);

				CloudMessaging.Data senderFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("sender")
					.category("eatSignal")
					.tryCount("3")
					.eatSignalSeq(eatSignalDto.getEatSignalSeq().toString())
					.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
					.senderSelected(signalInfo.getSenderTertiarySelected())
					.recipientSelected(eatSignalDto.getRecipientTertiarySelected())
					.termination("true")
					.result(signalInfo.getTertiaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				CloudMessaging.Data recipientFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("recipient")
					.category("eatSignal")
					.tryCount("3")
					.eatSignalSeq(eatSignalDto.getEatSignalSeq().toString())
					.tempSignalSeq("0")
					.senderSelected(signalInfo.getSenderTertiarySelected())
					.recipientSelected(eatSignalDto.getRecipientTertiarySelected())
					.termination("true")
					.result(signalInfo.getTertiaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				cloudMessagingService.sendMessageTo(senderDevice, fcmTitle, fcmBody, senderFcmData, androidConfig, apnsConfig);
				cloudMessagingService.sendMessageTo(recipientDevice, fcmTitle, fcmBody, recipientFcmData, androidConfig, apnsConfig);

				NotificationDto notificationDto = new NotificationDto();
				notificationDto.setUsername(eatSignalDto.getUsername());
				notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
				notificationDto.setType(NotificationType.SIGNAL);
				notificationDto.setMessage(NotificationMessage.EAT_SIGNAL.getText());

				memberMapper.setNotification(notificationDto);

				return result;
			} else {
				throw new Exception();
			}
		} else {
			//TODO
			throw new Exception();
		}
	}


}
