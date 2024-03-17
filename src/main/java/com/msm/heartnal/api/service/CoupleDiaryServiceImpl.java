package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.CloudMessaging;
import com.msm.heartnal.core.dto.CoupleDiaryDto;
import com.msm.heartnal.core.dto.NotificationDto;
import com.msm.heartnal.core.enums.NotificationMessage;
import com.msm.heartnal.core.enums.NotificationType;
import com.msm.heartnal.core.mapper.CoupleDiaryMapper;
import com.msm.heartnal.core.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * @author 최대희
 * @since 2021-12-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoupleDiaryServiceImpl implements CoupleDiaryService {
	private final CoupleDiaryMapper coupleDiaryMapper;

	private final MemberMapper memberMapper;
	private final CloudMessagingService cloudMessagingService;

	@Override
	public boolean checkCoupleDiaryAuthority(Long diarySeq, Long writerMemberSeq) {
		CoupleDiaryDto coupleDiaryInfo = coupleDiaryMapper.getCoupleDiary(diarySeq);
		return coupleDiaryInfo.getWriterMemberSeq() == writerMemberSeq;
	}

	@Override
	@Transactional
	public boolean setCoupleDiary(CoupleDiaryDto coupleDiaryDto) throws IOException {
		try {
			String result = coupleDiaryDto.getContents().replaceAll("\\p{Z}", "[]");
			String wordBreak = result.replaceAll("(\r\n|\r|\n|\n\r)", "{}");
			coupleDiaryDto.setContents(result);

			MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(coupleDiaryDto.getWriterMemberSeq());

			NotificationDto notificationDto = new NotificationDto();
			notificationDto.setMemberSeq(memberDao.getMemberSeq());
			notificationDto.setCoupleMemberSeq(memberDao.getCoupleMemberSeq());
			notificationDto.setType(NotificationType.COUPLE_DIARY);
			notificationDto.setMessage(NotificationMessage.COUPLE_DIARY.getText());

			boolean notificationResult = memberMapper.setNotification(notificationDto);

			String titleMessage = "커플 다이어리가 등록되었어요.";
			String bodyMessage = "";

			CloudMessaging.Data fcmData = CloudMessaging.Data.builder()
				.isSignal("false")
				.build();
			CloudMessaging.Android androidConfig = CloudMessaging.Android.builder()
				.priority("high")
				.build();
			CloudMessaging.Apns apnsConfig = CloudMessaging.Apns.builder()
				.priority("5")
				.content_available(true)
				.build();
			cloudMessagingService.sendMessageTo(memberDao.getCoupleDeviceToken(), titleMessage, bodyMessage, fcmData, androidConfig, apnsConfig);

			return coupleDiaryMapper.setCoupleDiary(coupleDiaryDto);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public List<CoupleDiaryDto> getCoupleDiary(Long writerMemberSeq, String coupleCode) {
		List<CoupleDiaryDto> result = coupleDiaryMapper.getCoupleDiaryList(coupleCode);
		for (CoupleDiaryDto coupleDiaryDto : result) {
			String spaceChange = coupleDiaryDto.getContents().replaceAll("\\[\\]", " ");
			String wordBreak = spaceChange.replaceAll("\\{\\}", System.lineSeparator());
			coupleDiaryDto.setContents(wordBreak);

			if (coupleDiaryDto.getLikeMember1() == null && coupleDiaryDto.getLikeMember2() == null) {
				coupleDiaryDto.setLikeCount(0);
				coupleDiaryDto.setLikeYN(false);
			} else if (coupleDiaryDto.getLikeMember1() != null && coupleDiaryDto.getLikeMember2() == null) {
				coupleDiaryDto.setLikeCount(1);
				coupleDiaryDto.setLikeYN(coupleDiaryDto.getLikeMember1() == writerMemberSeq);
			} else if (coupleDiaryDto.getLikeMember1() == null && coupleDiaryDto.getLikeMember2() != null) {
				coupleDiaryDto.setLikeCount(1);
				coupleDiaryDto.setLikeYN(coupleDiaryDto.getLikeMember2() == writerMemberSeq);
			} else {
				coupleDiaryDto.setLikeCount(2);
				if (coupleDiaryDto.getLikeMember1() == writerMemberSeq || coupleDiaryDto.getLikeMember2() == writerMemberSeq) {
					coupleDiaryDto.setLikeYN(true);
				}
			}
		}
		return result;
	}

	@Override
	public boolean updateCoupleDiary(CoupleDiaryDto coupleDiaryDto) {
		String result = coupleDiaryDto.getContents().replaceAll("\\p{Z}", "[]");
		String wordBreak = result.replaceAll("(\r\n|\r|\n|\n\r)", "{}");
		coupleDiaryDto.setContents(result);

		return coupleDiaryMapper.updateCoupleDiary(coupleDiaryDto);
	}

	@Override
	public boolean deleteCoupleDiary(CoupleDiaryDto coupleDiaryDto) {
		return coupleDiaryMapper.deleteCoupleDiary(coupleDiaryDto);
	}

	@Override
	public boolean pressLike(CoupleDiaryDto coupleDiaryDto) {
		CoupleDiaryDto coupleDiaryInfo = coupleDiaryMapper.getCoupleDiary(coupleDiaryDto.getDiarySeq());
		if (coupleDiaryInfo.getLikeMember1() == null && coupleDiaryInfo.getLikeMember2() == null) {
			coupleDiaryDto.setLikeMember1(coupleDiaryDto.getMemberSeq());
		} else if (coupleDiaryInfo.getLikeMember1() != null && coupleDiaryInfo.getLikeMember2() == null) {
			coupleDiaryDto.setLikeMember2(coupleDiaryDto.getMemberSeq());
		}
		return coupleDiaryMapper.pressLike(coupleDiaryDto);
	}
}
