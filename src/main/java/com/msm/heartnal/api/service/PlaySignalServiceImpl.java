package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.*;
import com.msm.heartnal.core.enums.NotificationMessage;
import com.msm.heartnal.core.enums.NotificationType;
import com.msm.heartnal.core.jwt.enums.CommonException;
import com.msm.heartnal.core.mapper.MemberMapper;
import com.msm.heartnal.core.mapper.PlaySignalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author 최대희
 * @since 2021-07-14
 */
@Service
@RequiredArgsConstructor
public class PlaySignalServiceImpl implements PlaySignalService {
	private final PlaySignalMapper playSignalMapper;
	private final MemberMapper memberMapper;
	private final CloudMessagingService cloudMessagingService;

	@Override
	public int senderPlaySignal(PlaySignalDto playSignalDto) throws CommonException, Exception {
		CloudMessaging.Android androidConfig = CloudMessaging.Android.builder()
			.priority("high")
			.build();
		CloudMessaging.Apns apnsConfig = CloudMessaging.Apns.builder()
			.priority("5")
			.content_available(true)
			.build();

		// 첫번째 시도일 경우 insert
		if (playSignalDto.getPlaySignalSeq() == null) {
			MemberDao memberDao = memberMapper.getMemberInfoByUsername(playSignalDto.getUsername());
			playSignalDto.setDeviceToken(memberDao.getCoupleDeviceToken());
			playSignalDto.setTitle(NotificationType.PLAY_SIGNAL.getText());
			playSignalDto.setBody(NotificationMessage.SIGNAL.getText());

			playSignalDto.setSenderMemberSeq(memberDao.getMemberSeq());
			playSignalDto.setRecipientMemberSeq(memberDao.getCoupleMemberSeq());
			playSignalDto.setCoupleCode(memberDao.getCoupleCode());

			// 제너레이트 키 생성
			int result = playSignalMapper.senderPlaySignal(playSignalDto);

			// 키 확보
			// recipient 에게 eatPrimarySignalSeq 전달
			// fcm 데이터에 포함해서 전달 후 flutter 에서 받아서 처리
			if (result == 1) {
				TempSignalDto tempSignalDto = new TempSignalDto();
				tempSignalDto.setCategory("playSignal");
				tempSignalDto.setSignalSeq(playSignalDto.getPlaySignalSeq());
				tempSignalDto.setPosition("recipient");
				tempSignalDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				tempSignalDto.setTryCount(1);
				tempSignalDto.setTermination(false);

				memberMapper.setTempSignal(tempSignalDto);

				CloudMessaging.Data fcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("recipient")
					.category("playSignal")
					.tryCount("1")
					.playSignalSeq(playSignalDto.getPlaySignalSeq().toString())
					.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
					.senderSelected(playSignalDto.getSenderPrimarySelected())
					.termination("false")
					.build();

				cloudMessagingService.sendMessageTo(playSignalDto.getDeviceToken(), playSignalDto.getTitle(), playSignalDto.getBody(), fcmData, androidConfig, apnsConfig);

				NotificationDto notificationDto = new NotificationDto();
				notificationDto.setUsername(playSignalDto.getUsername());
				notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
				notificationDto.setType(NotificationType.SIGNAL);
				notificationDto.setMessage(NotificationMessage.PLAY_SIGNAL.getText());

				memberMapper.setNotification(notificationDto);

				return result;
			} else {
				throw new Exception();
			}

		} else {
			// 두번째 이상 시도일 경우 update
			PlaySignalDto signalInfo = playSignalMapper.getPlaySignalBySignalSeq(playSignalDto.getPlaySignalSeq());
			MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(signalInfo.getSenderMemberSeq());

			// 두번째 시도
			if (signalInfo.getPrimaryResult() != null && signalInfo.getSecondaryResult() == null && signalInfo.getFinalResult() == null) {
				signalInfo.setSenderSecondarySelected(playSignalDto.getSenderSecondarySelected());

				String title = NotificationType.PLAY_SIGNAL.getText();
				String body = NotificationMessage.SIGNAL.getText();

				int result = playSignalMapper.senderPlaySignalUpdate(signalInfo);
				if (result == 1) {
					TempSignalDto tempSignalDto = new TempSignalDto();
					tempSignalDto.setCategory("playSignal");
					tempSignalDto.setSignalSeq(playSignalDto.getPlaySignalSeq());
					tempSignalDto.setPosition("recipient");
					tempSignalDto.setMemberSeq(memberDao.getCoupleMemberSeq());
					tempSignalDto.setTryCount(2);
					tempSignalDto.setTermination(false);

					memberMapper.setTempSignal(tempSignalDto);

					CloudMessaging.Data fcmData = CloudMessaging.Data.builder()
						.isSignal("true")
						.position("recipient")
						.category("playSignal")
						.tryCount("2")
						.playSignalSeq(playSignalDto.getPlaySignalSeq().toString())
						.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
						.senderSelected(signalInfo.getSenderPrimarySelected())
						.recipientSelected(signalInfo.getRecipientPrimarySelected())
						.termination("false")
						.build();

					cloudMessagingService.sendMessageTo(memberDao.getCoupleDeviceToken(), title, body, fcmData, androidConfig, apnsConfig);

					NotificationDto notificationDto = new NotificationDto();
					notificationDto.setUsername(playSignalDto.getUsername());
					notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
					notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
					notificationDto.setType(NotificationType.SIGNAL);
					notificationDto.setMessage(NotificationMessage.PLAY_SIGNAL.getText());

					memberMapper.setNotification(notificationDto);

					return result;
				} else {
					throw new Exception();
				}

			}
			// 세번째 시도
			else if (signalInfo.getPrimaryResult() != null && signalInfo.getSecondaryResult() != null && signalInfo.getFinalResult() == null) {
				signalInfo.setSenderTertiarySelected(playSignalDto.getSenderTertiarySelected());

				String title = NotificationType.PLAY_SIGNAL.getText();
				String body = NotificationMessage.SIGNAL.getText();

				int result = playSignalMapper.senderPlaySignalUpdate(signalInfo);
				if (result == 1) {
					TempSignalDto tempSignalDto = new TempSignalDto();
					tempSignalDto.setCategory("playSignal");
					tempSignalDto.setSignalSeq(playSignalDto.getPlaySignalSeq());
					tempSignalDto.setPosition("recipient");
					tempSignalDto.setMemberSeq(memberDao.getCoupleMemberSeq());
					tempSignalDto.setTryCount(3);
					tempSignalDto.setTermination(false);

					memberMapper.setTempSignal(tempSignalDto);

					CloudMessaging.Data fcmData = CloudMessaging.Data.builder()
						.isSignal("true")
						.position("recipient")
						.category("playSignal")
						.tryCount("3")
						.playSignalSeq(playSignalDto.getPlaySignalSeq().toString())
						.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
						.senderSelected(signalInfo.getSenderSecondarySelected())
						.recipientSelected(signalInfo.getRecipientSecondarySelected())
						.termination("false")
						.build();

					cloudMessagingService.sendMessageTo(memberDao.getCoupleDeviceToken(), title, body, fcmData, androidConfig, apnsConfig);

					NotificationDto notificationDto = new NotificationDto();
					notificationDto.setUsername(playSignalDto.getUsername());
					notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
					notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
					notificationDto.setType(NotificationType.SIGNAL);
					notificationDto.setMessage(NotificationMessage.PLAY_SIGNAL.getText());

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
	public int recipientPlaySignal(PlaySignalDto playSignalDto) throws CommonException, Exception {
		PlaySignalDto signalInfo = playSignalMapper.getPlaySignalBySignalSeq(playSignalDto.getPlaySignalSeq());
		signalInfo.setPlaySignalSeq(playSignalDto.getPlaySignalSeq());

		MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(signalInfo.getSenderMemberSeq());

		String senderDevice = memberDao.getMyDeviceToken();
		String fcmTitle = NotificationType.RESULT_PLAY_SIGNAL.getText();
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
		if (signalInfo.getRecipientPrimarySelected() == null && signalInfo.getRecipientSecondarySelected() == null && signalInfo.getRecipientTertiarySelected() == null) {
			signalInfo.setRecipientPrimarySelected(playSignalDto.getRecipientPrimarySelected());

			if (signalInfo.getSenderPrimarySelected().equals(playSignalDto.getRecipientPrimarySelected())) {
				signalInfo.setPrimaryResult(true);
				signalInfo.setFinalResult(true);

				signalInfo.setFinalResultItem(playSignalDto.getRecipientPrimarySelected());

				resultSelected = signalInfo.getFinalResultItem();

			} else {
				signalInfo.setPrimaryResult(false);

				resultSelected = "";
			}

			int result = playSignalMapper.recipientPlaySignal(signalInfo);
			if (result == 1) {
				TempSignalDto tempSignalDto = new TempSignalDto();
				tempSignalDto.setCategory("playSignal");
				tempSignalDto.setSignalSeq(playSignalDto.getPlaySignalSeq());
				tempSignalDto.setPosition("sender");
				tempSignalDto.setMemberSeq(signalInfo.getSenderMemberSeq());
				tempSignalDto.setTryCount(1);
				tempSignalDto.setTermination(true);

				memberMapper.setTempSignal(tempSignalDto);

				CloudMessaging.Data senderFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("sender")
					.category("playSignal")
					.tryCount("1")
					.playSignalSeq(playSignalDto.getPlaySignalSeq().toString())
					.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
					.senderSelected(signalInfo.getSenderPrimarySelected())
					.recipientSelected(playSignalDto.getRecipientPrimarySelected())
					.termination("true")
					.result(signalInfo.getPrimaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				CloudMessaging.Data recipientFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("recipient")
					.category("playSignal")
					.tryCount("1")
					.playSignalSeq(playSignalDto.getPlaySignalSeq().toString())
					.tempSignalSeq("0")
					.senderSelected(signalInfo.getSenderPrimarySelected())
					.recipientSelected(playSignalDto.getRecipientPrimarySelected())
					.termination("true")
					.result(signalInfo.getPrimaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				cloudMessagingService.sendMessageTo(memberDao.getMyDeviceToken(), fcmTitle, fcmBody, senderFcmData, androidConfig, apnsConfig);
				cloudMessagingService.sendMessageTo(memberDao.getCoupleDeviceToken(), fcmTitle, fcmBody, recipientFcmData, androidConfig, apnsConfig);

				NotificationDto notificationDto = new NotificationDto();
				notificationDto.setUsername(playSignalDto.getUsername());
				notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
				notificationDto.setType(NotificationType.SIGNAL);
				notificationDto.setMessage(NotificationMessage.PLAY_SIGNAL.getText());

				memberMapper.setNotification(notificationDto);

				return result;
			} else {
				throw new Exception();
			}
		}
		//
		else if (signalInfo.getRecipientPrimarySelected() != null && signalInfo.getRecipientSecondarySelected() == null) {
			signalInfo.setRecipientSecondarySelected(playSignalDto.getRecipientSecondarySelected());

			if (signalInfo.getSenderSecondarySelected().equals(playSignalDto.getRecipientSecondarySelected())) {
				signalInfo.setSecondaryResult(true);
				signalInfo.setFinalResult(true);

				signalInfo.setFinalResultItem(playSignalDto.getRecipientSecondarySelected());

				resultSelected = signalInfo.getFinalResultItem();

			} else {
				signalInfo.setSecondaryResult(false);

				resultSelected = "";
			}

			int result = playSignalMapper.recipientPlaySignal(signalInfo);
			if (result == 1) {
				TempSignalDto tempSignalDto = new TempSignalDto();
				tempSignalDto.setCategory("playSignal");
				tempSignalDto.setSignalSeq(playSignalDto.getPlaySignalSeq());
				tempSignalDto.setPosition("sender");
				tempSignalDto.setMemberSeq(signalInfo.getSenderMemberSeq());
				tempSignalDto.setTryCount(2);
				tempSignalDto.setTermination(true);

				memberMapper.setTempSignal(tempSignalDto);

				CloudMessaging.Data senderFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("sender")
					.category("playSignal")
					.tryCount("2")
					.playSignalSeq(playSignalDto.getPlaySignalSeq().toString())
					.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
					.senderSelected(signalInfo.getSenderSecondarySelected())
					.recipientSelected(playSignalDto.getRecipientSecondarySelected())
					.termination("true")
					.result(signalInfo.getSecondaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				CloudMessaging.Data recipientFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("recipient")
					.category("playSignal")
					.tryCount("2")
					.playSignalSeq(playSignalDto.getPlaySignalSeq().toString())
					.tempSignalSeq("0")
					.senderSelected(signalInfo.getSenderSecondarySelected())
					.recipientSelected(playSignalDto.getRecipientSecondarySelected())
					.termination("true")
					.result(signalInfo.getSecondaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				cloudMessagingService.sendMessageTo(memberDao.getMyDeviceToken(), fcmTitle, fcmBody, senderFcmData, androidConfig, apnsConfig);
				cloudMessagingService.sendMessageTo(memberDao.getCoupleDeviceToken(), fcmTitle, fcmBody, recipientFcmData, androidConfig, apnsConfig);

				NotificationDto notificationDto = new NotificationDto();
				notificationDto.setUsername(playSignalDto.getUsername());
				notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
				notificationDto.setType(NotificationType.SIGNAL);
				notificationDto.setMessage(NotificationMessage.PLAY_SIGNAL.getText());

				memberMapper.setNotification(notificationDto);

				return result;
			} else {
				throw new Exception();
			}
		}
		//
		else if (signalInfo.getRecipientPrimarySelected() != null && signalInfo.getRecipientSecondarySelected() != null && signalInfo.getRecipientTertiarySelected() == null) {
			signalInfo.setRecipientTertiarySelected(playSignalDto.getRecipientTertiarySelected());

			if (signalInfo.getSenderTertiarySelected().equals(playSignalDto.getRecipientTertiarySelected())) {
				signalInfo.setTertiaryResult(true);
				signalInfo.setFinalResult(true);

				signalInfo.setFinalResultItem(playSignalDto.getRecipientTertiarySelected());

				resultSelected = signalInfo.getFinalResultItem();
			} else {
				signalInfo.setTertiaryResult(false);
				signalInfo.setFinalResult(false);

				resultSelected = "";
			}

			int result = playSignalMapper.recipientPlaySignal(signalInfo);
			if (result == 1) {
				TempSignalDto tempSignalDto = new TempSignalDto();
				tempSignalDto.setCategory("playSignal");
				tempSignalDto.setSignalSeq(playSignalDto.getPlaySignalSeq());
				tempSignalDto.setPosition("sender");
				tempSignalDto.setMemberSeq(signalInfo.getSenderMemberSeq());
				tempSignalDto.setTryCount(3);
				tempSignalDto.setTermination(true);

				memberMapper.setTempSignal(tempSignalDto);

				CloudMessaging.Data senderFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("sender")
					.category("playSignal")
					.tryCount("3")
					.playSignalSeq(playSignalDto.getPlaySignalSeq().toString())
					.tempSignalSeq(tempSignalDto.getTempSignalSeq().toString())
					.senderSelected(signalInfo.getSenderTertiarySelected())
					.recipientSelected(playSignalDto.getRecipientTertiarySelected())
					.termination("true")
					.result(signalInfo.getTertiaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				CloudMessaging.Data recipientFcmData = CloudMessaging.Data.builder()
					.isSignal("true")
					.position("recipient")
					.category("playSignal")
					.tryCount("3")
					.playSignalSeq(playSignalDto.getPlaySignalSeq().toString())
					.tempSignalSeq("0")
					.senderSelected(signalInfo.getSenderTertiarySelected())
					.recipientSelected(playSignalDto.getRecipientTertiarySelected())
					.termination("true")
					.result(signalInfo.getTertiaryResult().toString())
					.resultSelected(resultSelected)
					.build();

				cloudMessagingService.sendMessageTo(memberDao.getMyDeviceToken(), fcmTitle, fcmBody, senderFcmData, androidConfig, apnsConfig);
				cloudMessagingService.sendMessageTo(memberDao.getCoupleDeviceToken(), fcmTitle, fcmBody, recipientFcmData, androidConfig, apnsConfig);

				NotificationDto notificationDto = new NotificationDto();
				notificationDto.setUsername(playSignalDto.getUsername());
				notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
				notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
				notificationDto.setType(NotificationType.SIGNAL);
				notificationDto.setMessage(NotificationMessage.PLAY_SIGNAL.getText());

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
