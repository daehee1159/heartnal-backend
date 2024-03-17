package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.*;
import com.msm.heartnal.core.dto.inquiry.InquiryDto;
import com.msm.heartnal.core.dto.signal.TodaySignalDto;
import com.msm.heartnal.core.enums.MemberStatus;
import com.msm.heartnal.core.enums.NotificationMessage;
import com.msm.heartnal.core.enums.NotificationType;
import com.msm.heartnal.core.enums.ResultCode;
import com.msm.heartnal.core.mapper.*;
import com.msm.heartnal.util.dday.DDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author 최대희
 * @since 2021-06-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
	private final MemberMapper memberMapper;

	private final PasswordEncoder passwordEncoder;

	private final EatSignalMapper eatSignalMapper;
	private final PlaySignalMapper playSignalMapper;
	private final SignalMapper signalMapper;
	private final CoupleDiaryMapper coupleDiaryMapper;
	private final DailySignalMapper dailySignalMapper;

	private final CalendarMapper calendarMapper;

	private final CloudMessagingService cloudMessagingService;

	private final DDayService dDayService;

	@Override
	public MemberStatus memberRegistrationCheck(String email) {
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(email);
		if (memberDao == null) {
			return MemberStatus.UNSUBSCRIBED;
		} else {
			return memberDao.getStatus();
		}
	}

	@Override
	public Long getMemberSeqByEmail(String email) {
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(email);
		return memberDao.getMemberSeq();
	}

	@Override
	public String getCoupleCodeByMemberSeq(Long memberSeq) {
		MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(memberSeq);
		return memberDao.getCoupleCode();
	}

	@Override
	public Object memberRegistration(MemberDto memberDto) {
		String encodedPassword = passwordEncoder.encode(memberDto.getEmail());

		MemberDao memberDao = memberMapper.getMemberInfoByUsername(memberDto.getEmail());

		if (memberDto.getCoupleDeviceToken().equals("true")) {
			// 이미 이전에 검증을 마치고 오기 때문에 아래 mapper 에서 2개의 return 이 나올 수 없음, 그래서 이 로직이 가능함
			MemberDao coupleInfo = memberMapper.getMemberInfoByCoupleCode(memberDto.getCoupleCode());
			memberDto.setCoupleDeviceToken(coupleInfo.getMyDeviceToken());
			memberDto.setCoupleMemberSeq(coupleInfo.getMemberSeq());
			memberDto.setCoupleNickName(coupleInfo.getNickName());
			memberDto.setExpression("grinHearts");
			memberDto.setCoupleRegDt(coupleInfo.getCoupleRegDt());

			if (memberDao == null) {
				MemberDao saveData = MemberDao.of(memberDto, encodedPassword);
				if (memberMapper.memberRegistration(saveData)) {
					// 여기서 원래 먼저 가입한 회원에게도 상대의 coupleDeviceToken 을 저장해줘야함
					boolean coupleUpdateResult = memberMapper.updateCoupleInfo(saveData.getMemberSeq(), memberDto.getNickName(), coupleInfo.getCoupleCode(), memberDto.getMyDeviceToken(), coupleInfo.getCoupleRegDt(), coupleInfo.getUsername());
					NotificationDto notificationDto = new NotificationDto();
					notificationDto.setUsername(saveData.getUsername());
					notificationDto.setMemberSeq(saveData.getMemberSeq());
					notificationDto.setCoupleMemberSeq(coupleInfo.getMemberSeq());
					notificationDto.setType(NotificationType.COMMON);
					notificationDto.setMessage(NotificationMessage.REGISTRATION.getText());
					memberMapper.setNotification(notificationDto);

					return saveData.getMemberSeq();
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			if (memberDao == null) {
				memberDto.setCoupleMemberSeq(0L);
				memberDto.setCoupleNickName("null");
				memberDto.setExpression("grinHearts");

				MemberDao saveData = MemberDao.of(memberDto, encodedPassword);
				if (memberMapper.memberRegistration(saveData)) {

					NotificationDto notificationDto = new NotificationDto();
					notificationDto.setUsername(saveData.getUsername());
					notificationDto.setMemberSeq(saveData.getMemberSeq());
					notificationDto.setCoupleMemberSeq(0L);
					notificationDto.setType(NotificationType.COMMON);
					notificationDto.setMessage(NotificationMessage.REGISTRATION.getText());
					memberMapper.setNotification(notificationDto);

					return saveData.getMemberSeq();
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean iosMemberRegistration(IosMemberDto iosMemberDto) {
		return memberMapper.iosMemberRegistration(iosMemberDto);
	}

	@Override
	public String getIosMember(String identifier) {
		IosMemberDto result = memberMapper.getIosMember(identifier);

		if (result == null) {
			return "not found";
		} else {
			return memberMapper.getIosMember(identifier).getEmail();
		}
	}

	@Override
	public String getFCMToken(Long memberSeq) {
		return memberMapper.getFCMToken(memberSeq);
	}

	@Override
	public boolean isCheckCoupleConnect(String username) {
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(username);
		return memberDao.getCoupleMemberSeq() != null && memberDao.getCoupleMemberSeq() != 0L;
	}

	@Override
	public boolean changedDeviceToken(MemberDto memberDto) {
		MemberDao myInfo = memberMapper.getMemberInfoByUsername(memberDto.getEmail());
		if (myInfo.getCoupleMemberSeq() != null && myInfo.getCoupleMemberSeq() != 0L) {
			MemberDao coupleInfo = memberMapper.getMemberInfoBySenderMemberSeq(myInfo.getCoupleMemberSeq());
			boolean myResult = memberMapper.changedDeviceToken(memberDto.getEmail(), memberDto.getMyDeviceToken(), coupleInfo.getMyDeviceToken());
			boolean coupleResult = memberMapper.changedDeviceToken(coupleInfo.getUsername(), coupleInfo.getMyDeviceToken(), memberDto.getMyDeviceToken());
			return myResult && coupleResult;
		} else {
			return memberMapper.changedDeviceToken(memberDto.getEmail(), memberDto.getMyDeviceToken(), null);
		}
	}

	@Override
	public boolean memberInitialization(DeleteAccountDto deleteAccountDto) {
		return memberMapper.deleteAccount(deleteAccountDto);
	}

	@Override
	public MemberDao getMemberInfoByUsername(String username) {
		return memberMapper.getMemberInfoByUsername(username);
	}

	@Override
	public boolean hasAdminRole() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		return authorities.stream().anyMatch(o -> o.getAuthority().equals("ROLE_ADMIN"));
	}

	@Override
	public boolean hasUserRole() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		return authorities.stream().anyMatch(o -> o.getAuthority().equals("ROLE_USER"));
	}

	@Override
	public boolean setCalendar(CalendarRequestDto calendarRequestDto) throws IOException {
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(calendarRequestDto.getUsername());

		calendarRequestDto.setCoupleCode(memberDao.getCoupleCode());

		boolean setCalendarResult = memberMapper.setCalendar(calendarRequestDto);
		NotificationDto notificationDto = new NotificationDto();
		notificationDto.setUsername(calendarRequestDto.getUsername());
		notificationDto.setType(NotificationType.CALENDAR);
		notificationDto.setCalendarDt(calendarRequestDto.getStartDt());
		boolean notificationResult = setNotification(notificationDto);

		String calendarDt = "";
		String year = calendarRequestDto.getStartDt().substring(0, 4) + "년 ";
		String month = calendarRequestDto.getStartDt().substring(5, 7) + "월 ";
		String day = calendarRequestDto.getStartDt().substring(8, 10) + "일";

		String titleMessage = NotificationType.HEARTNAL.toString();
		String bodyMessage = year + month + day + "에 " + NotificationMessage.CALENDAR.getText();

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
		cloudMessagingService.sendMessageTo(memberDao.getMyDeviceToken(), titleMessage, bodyMessage, fcmData, androidConfig, apnsConfig);

		return setCalendarResult;
	}

	@Override
	public List<CalendarResponseDto> getCalendar(String username) {
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(username);

		return memberMapper.getCalendar(memberDao.getCoupleCode());
	}

	@Override
	public boolean deleteCalendar(CalendarRequestDto calendarRequestDto) {
		return memberMapper.deleteCalendar(calendarRequestDto);
	}

	@Override
	public CheckMyTurnDto checkMyTurn(String category, Long signalSeq, Long memberSeq) {
		// 각 시그널 별 분기처리
		CheckMyTurnDto result = new CheckMyTurnDto();

		if (signalSeq == 0L) {
			result.setMyTurn(false);
		} else {
			switch (category) {
				case "eatSignal":
					EatSignalDto eatSignalDto = eatSignalMapper.getEatSignalBySignalSeq(signalSeq);
					result.setCategory("eatSignal");
					if (Objects.equals(eatSignalDto.getSenderMemberSeq(), memberSeq)) {
						// position == sender
						result.setMyPosition("sender");
						if (eatSignalDto.getPrimaryResult() == null) {
							// 이러면 recipient 차례인데 아직 상대가 답을 안준거임
							result.setMyTurn(false);
							result.setTryCount(1);
						} else if (eatSignalDto.getPrimaryResult() != null && eatSignalDto.getSecondaryResult() == null) {
							result.setMyTurn(false);
							result.setTryCount(2);
						} else if (eatSignalDto.getPrimaryResult() != null && eatSignalDto.getSecondaryResult() != null && eatSignalDto.getTertiaryResult() == null) {
							result.setMyTurn(false);
							result.setTryCount(3);
						} else {
							result.setMyTurn(true);
						}
					} else if (Objects.equals(eatSignalDto.getRecipientMemberSeq(), memberSeq)) {
						result.setMyPosition("recipient");
						if (eatSignalDto.getPrimaryResult() != null && eatSignalDto.getSenderSecondarySelected() == null) {
							// finalResult 가 false 인것을 가져오는지 먼저 확인 필요
							result.setMyTurn(false);
							result.setTryCount(2);
						} else if (eatSignalDto.getPrimaryResult() != null && eatSignalDto.getSecondaryResult() != null && eatSignalDto.getSenderTertiarySelected() == null) {
							result.setMyTurn(false);
							result.setTryCount(3);
						} else {
							result.setMyTurn(true);
						}
					}
					break;
				case "playSignal":
					PlaySignalDto playSignalDto = playSignalMapper.getPlaySignalBySignalSeq(signalSeq);
					result.setCategory("playSignal");
					if (Objects.equals(playSignalDto.getSenderMemberSeq(), memberSeq)) {
						result.setMyPosition("sender");
						if (playSignalDto.getPrimaryResult() == null) {
							result.setMyTurn(false);
							result.setTryCount(1);
						} else if (playSignalDto.getPrimaryResult() != null && playSignalDto.getSecondaryResult() == null) {
							result.setMyTurn(false);
							result.setTryCount(2);
						} else if (playSignalDto.getPrimaryResult() != null && playSignalDto.getSecondaryResult() != null && playSignalDto.getTertiaryResult() == null) {
							result.setMyTurn(false);
							result.setTryCount(3);
						} else {
							result.setMyTurn(true);
						}
					} else if (Objects.equals(playSignalDto.getRecipientMemberSeq(), memberSeq)) {
						result.setMyPosition("recipient");
						if (playSignalDto.getPrimaryResult() != null && playSignalDto.getSenderSecondarySelected() == null) {
							result.setMyTurn(false);
							result.setTryCount(2);
						} else if (playSignalDto.getPrimaryResult() != null && playSignalDto.getSecondaryResult() != null && playSignalDto.getSenderTertiarySelected() == null) {
							result.setMyTurn(false);
							result.setTryCount(3);
						} else {
							result.setMyTurn(true);
						}
					}
					break;
			}
		}
		return result;
	}

	@Override
	public List<TempSignalDto> getTempSignal(Long memberSeq) {
		return memberMapper.getTempSignal(memberSeq);
	}

	@Override
	public boolean deleteTempSignal(TempSignalDto tempSignalDto) {
		return memberMapper.deleteTempSignal(tempSignalDto);
	}

	@Override
	public CoupleUnResolvedSignalDto coupleUnResolvedSignal(String coupleCode, Long memberSeq) {
		CoupleUnResolvedSignalDto coupleUnResolvedSignalDto = new CoupleUnResolvedSignalDto();

		Long eatSignalSeq = memberMapper.coupleUnResolvedEatSignal(coupleCode);
		Long playSignalSeq = memberMapper.coupleUnResolvedPlaySignal(coupleCode);

		// 오늘 보낸 메시지가 있는지 확인
		List<MessageOfTheDayDto> messageOfTheDayList = dailySignalMapper.getMessageOfTheDayBySenderSeq(memberSeq);
		Collections.reverse(messageOfTheDayList);
		LocalDate today = LocalDate.now();
		boolean termination = false;

		for (MessageOfTheDayDto messageofTheDayDto : messageOfTheDayList) {
			LocalDate compareDate = LocalDate.parse(messageofTheDayDto.getRegDt().substring(0, 10));
			if (today.getYear() == compareDate.getYear() && today.getMonth() == compareDate.getMonth() && today.getDayOfMonth() == compareDate.getDayOfMonth()) {
				coupleUnResolvedSignalDto.setMessageOfTheDaySeq(messageofTheDayDto.getMessageOfTheDaySeq());
				termination = true;
				break;
			}
		}

		// 오늘의 시그널 오늘 보낸 이력이 있는지 확인
		// 사실 여기는 todaySignalDto 가 null 이냐 아니냐가 중요한거라 나머지 로직은 필요 없음, 어짜피 Front 에서 다시 내 턴인지 아닌지 확인해야함
		TodaySignalDto todaySignalDto = dailySignalMapper.getCheckTodaySignal(coupleCode, memberSeq);
		if (todaySignalDto == null) {
			coupleUnResolvedSignalDto.setTodaySignalSeq(0L);
		} else {
			coupleUnResolvedSignalDto.setTodaySignalSeq(todaySignalDto.getTodaySignalSeq());
		}

		if (!termination) {
			coupleUnResolvedSignalDto.setMessageOfTheDaySeq(0L);
		}

		if (eatSignalSeq == null) {
			coupleUnResolvedSignalDto.setEatSignalSeq(0L);
		} else {
			coupleUnResolvedSignalDto.setEatSignalSeq(eatSignalSeq);
		}

		if (playSignalSeq == null) {
			coupleUnResolvedSignalDto.setPlaySignalSeq(0L);
		} else {
			coupleUnResolvedSignalDto.setPlaySignalSeq(playSignalSeq);
		}

		if (eatSignalSeq == null && playSignalSeq == null && coupleUnResolvedSignalDto.getMessageOfTheDaySeq() == 0 && coupleUnResolvedSignalDto.getTodaySignalSeq() == 0) {
			coupleUnResolvedSignalDto.setHasUnResolved(false);
		} else {
			coupleUnResolvedSignalDto.setHasUnResolved(true);
		}

		return coupleUnResolvedSignalDto;
	}

	@Override
	public HasUnResolvedSignal hasUnResolvedSignal(String username) {
		HasUnResolvedSignal hasUnResolvedSignal = new HasUnResolvedSignal();

		// 일단 username 을 통해 memberSeq 를 가져옴
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(username);
		Long memberSeq = memberDao.getMemberSeq();

		EatSignalDto senderEatSignal = memberMapper.hasUnresolvedSenderEatSignal(memberSeq);

		// EatSignal Sender
		if (senderEatSignal == null) {
			hasUnResolvedSignal.setSenderEatSignalSeq(0L);
		} else if (senderEatSignal.getRecipientPrimarySelected() == null) {
			hasUnResolvedSignal.setSenderEatSignalSeq(0L);
		} else if (senderEatSignal.getRecipientPrimarySelected() != null && senderEatSignal.getRecipientSecondarySelected() == null && senderEatSignal.getSenderSecondarySelected() == null) {
			hasUnResolvedSignal.setSenderEatSignalSeq(senderEatSignal.getEatSignalSeq());
		} else if (senderEatSignal.getRecipientPrimarySelected() != null && senderEatSignal.getRecipientSecondarySelected() != null && senderEatSignal.getRecipientTertiarySelected() == null && senderEatSignal.getSenderTertiarySelected() == null) {
			hasUnResolvedSignal.setSenderEatSignalSeq(senderEatSignal.getEatSignalSeq());
		} else {
			hasUnResolvedSignal.setSenderEatSignalSeq(0L);
		}

		// PlaySignal Sender
		PlaySignalDto senderPlaySignal = memberMapper.hasUnresolvedSenderPlaySignal(memberSeq);
		if (senderPlaySignal == null) {
			hasUnResolvedSignal.setSenderPlaySignalSeq(0L);
		} else if (senderPlaySignal.getRecipientPrimarySelected() == null) {
			hasUnResolvedSignal.setSenderPlaySignalSeq(0L);
		} else if (senderPlaySignal.getRecipientPrimarySelected() != null && senderPlaySignal.getRecipientSecondarySelected() == null && senderPlaySignal.getSenderSecondarySelected() == null) {
			hasUnResolvedSignal.setSenderPlaySignalSeq(senderPlaySignal.getPlaySignalSeq());
		} else if (senderPlaySignal.getRecipientPrimarySelected() != null && senderPlaySignal.getRecipientSecondarySelected() != null && senderPlaySignal.getRecipientTertiarySelected() == null && senderPlaySignal.getSenderTertiarySelected() == null) {
			hasUnResolvedSignal.setSenderPlaySignalSeq(senderPlaySignal.getPlaySignalSeq());
		} else {
			hasUnResolvedSignal.setSenderPlaySignalSeq(0L);
		}

		// EatSignal Recipient
		EatSignalDto recipientEatSignal = memberMapper.hasUnresolvedRecipientEatSignal(memberSeq);

		if (recipientEatSignal == null) {
			hasUnResolvedSignal.setRecipientEatSignalSeq(0L);
		} else if (recipientEatSignal.getSenderPrimarySelected() != null && recipientEatSignal.getRecipientPrimarySelected() == null) {
			hasUnResolvedSignal.setRecipientEatSignalSeq(recipientEatSignal.getEatSignalSeq());
		} else if (recipientEatSignal.getSenderSecondarySelected() != null && recipientEatSignal.getRecipientSecondarySelected() == null) {
			hasUnResolvedSignal.setRecipientEatSignalSeq(recipientEatSignal.getEatSignalSeq());
		} else if (recipientEatSignal.getSenderTertiarySelected() != null && recipientEatSignal.getRecipientTertiarySelected() == null) {
			hasUnResolvedSignal.setRecipientEatSignalSeq(recipientEatSignal.getEatSignalSeq());
		} else {
			hasUnResolvedSignal.setRecipientEatSignalSeq(0L);
		}

		// PlaySignal Recipient
		PlaySignalDto recipientPlaySignal = memberMapper.hasUnresolvedRecipientPlaySignal(memberSeq);
		if (recipientPlaySignal == null) {
			hasUnResolvedSignal.setRecipientPlaySignalSeq(0L);
		} else if (recipientPlaySignal.getSenderPrimarySelected() != null && recipientPlaySignal.getRecipientPrimarySelected() == null) {
			hasUnResolvedSignal.setRecipientPlaySignalSeq(recipientPlaySignal.getPlaySignalSeq());
		} else if (recipientPlaySignal.getSenderSecondarySelected() != null && recipientPlaySignal.getRecipientSecondarySelected() == null) {
			hasUnResolvedSignal.setRecipientPlaySignalSeq(recipientPlaySignal.getPlaySignalSeq());
		} else if (recipientPlaySignal.getSenderTertiarySelected() != null && recipientPlaySignal.getRecipientTertiarySelected() == null) {
			hasUnResolvedSignal.setRecipientPlaySignalSeq(recipientPlaySignal.getPlaySignalSeq());
		} else {
			hasUnResolvedSignal.setRecipientPlaySignalSeq(0L);
		}

		if (hasUnResolvedSignal.getSenderEatSignalSeq() == 0L && hasUnResolvedSignal.getSenderPlaySignalSeq() == 0L &&
			hasUnResolvedSignal.getRecipientEatSignalSeq() == 0L && hasUnResolvedSignal.getRecipientPlaySignalSeq() == 0L) {
			hasUnResolvedSignal.setHasUnResolved(false);
		} else {
			hasUnResolvedSignal.setHasUnResolved(true);
		}

		return hasUnResolvedSignal;
	}

	@Override
	public UnResolvedSignalDto setUnResolvedSignal(UnResolvedSignalDto unResolvedSignalDto) {
		UnResolvedSignalDto result = new UnResolvedSignalDto();
		result.setPosition(unResolvedSignalDto.getPosition());
		result.setCategory(unResolvedSignalDto.getCategory());

		if (unResolvedSignalDto.getPosition().equals("sender")) {
			switch (unResolvedSignalDto.getCategory()) {
				case "eatSignal":
					EatSignalDto eatSignalDto = eatSignalMapper.getEatSignalBySignalSeq(unResolvedSignalDto.getEatSignalSeq());

					if (eatSignalDto.getFinalResult() == null) {
						// sender 는 첫번째 결과가 무조건 있을 수 밖에 없음, 첫번째 결과까지 나왔는데 두번째 시도를 하지 않은 경우
						if (eatSignalDto.getPrimaryResult() != null && eatSignalDto.getSecondaryResult() == null && eatSignalDto.getFinalResult() == null) {
							/**
							 * 사실 tryCount는 몇번째 시도 그대로 주는게 맞지만 Flutter에서 sender의 경우 시그널을 보낼 때 tryCount를 +1 해서 보내기 때문에 여기서 -1해서 줘야함
							 * */
							result.setTryCount(1);
							result.setEatSignalSeq(unResolvedSignalDto.getEatSignalSeq());
							result.setSenderSelected(eatSignalDto.getSenderPrimarySelected());
							result.setRecipientSelected(eatSignalDto.getRecipientPrimarySelected());
						}
						// 두번째 결과까지 나왔지만 마지막 시도를 하지 않은 경우
						else if (eatSignalDto.getPrimaryResult() != null && eatSignalDto.getSecondaryResult() != null && eatSignalDto.getFinalResult() == null) {
							/**
							 * 사실 tryCount는 몇번째 시도 그대로 주는게 맞지만 Flutter에서 sender의 경우 시그널을 보낼 때 tryCount를 +1 해서 보내기 때문에 여기서 -1해서 줘야함
							 * */
							result.setTryCount(2);
							result.setEatSignalSeq(unResolvedSignalDto.getEatSignalSeq());
							result.setSenderSelected(eatSignalDto.getSenderSecondarySelected());
							result.setRecipientSelected(eatSignalDto.getRecipientSecondarySelected());
						}
					} else {
						// 1차에 성공한 경우
						if (eatSignalDto.getPrimaryResult() && eatSignalDto.getFinalResult() != null) {
							result.setTryCount(1);
							result.setEatSignalSeq(unResolvedSignalDto.getEatSignalSeq());
							result.setSenderSelected(eatSignalDto.getSenderPrimarySelected());
							result.setRecipientSelected(eatSignalDto.getRecipientPrimarySelected());
						} else if (!eatSignalDto.getPrimaryResult() && eatSignalDto.getSecondaryResult() && eatSignalDto.getFinalResult() != null) {
							// 2차에 성공한 경우
							result.setTryCount(2);
							result.setEatSignalSeq(unResolvedSignalDto.getEatSignalSeq());
							result.setSenderSelected(eatSignalDto.getSenderSecondarySelected());
							result.setRecipientSelected(eatSignalDto.getRecipientSecondarySelected());
						} else if (!eatSignalDto.getPrimaryResult() && !eatSignalDto.getSecondaryResult() && eatSignalDto.getTertiaryResult() && eatSignalDto.getFinalResult() != null) {
							// 3차에 성공한 경우
							result.setTryCount(3);
							result.setEatSignalSeq(unResolvedSignalDto.getEatSignalSeq());
							result.setSenderSelected(eatSignalDto.getSenderTertiarySelected());
							result.setRecipientSelected(eatSignalDto.getRecipientTertiarySelected());
						}
					}

					break;
				case "playSignal":
					PlaySignalDto playSignalDto = playSignalMapper.getPlaySignalBySignalSeq(unResolvedSignalDto.getPlaySignalSeq());

					if (playSignalDto.getFinalResult() == null) {
						// sender 는 첫번째 결과가 무조건 있을 수 밖에 없음, 첫번째 결과가 나왔는데 두번째 시도를 하지 않은 경우
						if (playSignalDto.getPrimaryResult() != null && playSignalDto.getSecondaryResult() == null && playSignalDto.getFinalResult() == null) {
							/**
							 * 사실 tryCount는 몇번째 시도 그대로 주는게 맞지만 Flutter에서 sender의 경우 시그널을 보낼 때 tryCount를 +1 해서 보내기 때문에 여기서 -1해서 줘야함
							 * */
							result.setTryCount(1);
							result.setPlaySignalSeq(unResolvedSignalDto.getPlaySignalSeq());
							result.setSenderSelected(playSignalDto.getSenderPrimarySelected());
							result.setRecipientSelected(playSignalDto.getRecipientPrimarySelected());
						}
						// 두번째 결과까지 나왔지만 마지막 시도를 하지 않은 경우
						else if (playSignalDto.getPrimaryResult() != null && playSignalDto.getSecondaryResult() != null && playSignalDto.getFinalResult() == null) {
							/**
							 * 사실 tryCount는 몇번째 시도 그대로 주는게 맞지만 Flutter에서 sender의 경우 시그널을 보낼 때 tryCount를 +1 해서 보내기 때문에 여기서 -1해서 줘야함
							 * */
							result.setTryCount(2);
							result.setPlaySignalSeq(unResolvedSignalDto.getPlaySignalSeq());
							result.setSenderSelected(playSignalDto.getSenderSecondarySelected());
							result.setRecipientSelected(playSignalDto.getRecipientSecondarySelected());
						}
					} else {
						if (playSignalDto.getPrimaryResult() && playSignalDto.getFinalResult() != null) {
							result.setTryCount(1);
							result.setEatSignalSeq(unResolvedSignalDto.getEatSignalSeq());
							result.setSenderSelected(playSignalDto.getSenderPrimarySelected());
							result.setRecipientSelected(playSignalDto.getRecipientPrimarySelected());
						} else if (!playSignalDto.getPrimaryResult() && playSignalDto.getSecondaryResult() && playSignalDto.getFinalResult() != null) {
							result.setTryCount(2);
							result.setEatSignalSeq(unResolvedSignalDto.getEatSignalSeq());
							result.setSenderSelected(playSignalDto.getSenderSecondarySelected());
							result.setRecipientSelected(playSignalDto.getRecipientSecondarySelected());
						} else if (!playSignalDto.getPrimaryResult() && !playSignalDto.getSecondaryResult() && playSignalDto.getTertiaryResult() && playSignalDto.getFinalResult() != null) {
							result.setTryCount(3);
							result.setEatSignalSeq(unResolvedSignalDto.getEatSignalSeq());
							result.setSenderSelected(playSignalDto.getSenderTertiarySelected());
							result.setRecipientSelected(playSignalDto.getRecipientTertiarySelected());
						}
					}
					break;
			}
		} else if (unResolvedSignalDto.getPosition().equals("recipient")) {
			switch (unResolvedSignalDto.getCategory()) {
				case "eatSignal":
					EatSignalDto eatSignalDto = eatSignalMapper.getEatSignalBySignalSeq(unResolvedSignalDto.getEatSignalSeq());

					// 첫번째 시도인 경우
					if (eatSignalDto.getPrimaryResult() == null && eatSignalDto.getSecondaryResult() == null && eatSignalDto.getFinalResult() == null) {
						result.setTryCount(1);
						result.setEatSignalSeq(unResolvedSignalDto.getEatSignalSeq());
						result.setSenderSelected(eatSignalDto.getSenderPrimarySelected());
						result.setRecipientSelected(eatSignalDto.getRecipientPrimarySelected());
					}
					// 두번째 시도인 경우
					else if (eatSignalDto.getPrimaryResult() != null && eatSignalDto.getSecondaryResult() == null && eatSignalDto.getFinalResult() == null) {
						result.setTryCount(2);
						result.setEatSignalSeq(unResolvedSignalDto.getEatSignalSeq());
						result.setSenderSelected(eatSignalDto.getSenderPrimarySelected());
						result.setRecipientSelected(eatSignalDto.getRecipientPrimarySelected());
					} else if (eatSignalDto.getPrimaryResult() != null && eatSignalDto.getSecondaryResult() != null && eatSignalDto.getFinalResult() == null) {
						result.setTryCount(3);
						result.setEatSignalSeq(unResolvedSignalDto.getEatSignalSeq());
						result.setSenderSelected(eatSignalDto.getSenderSecondarySelected());
						result.setRecipientSelected(eatSignalDto.getRecipientSecondarySelected());
					}
					break;
				case "playSignal":
					PlaySignalDto playSignalDto = playSignalMapper.getPlaySignalBySignalSeq(unResolvedSignalDto.getPlaySignalSeq());

					// 첫번째 시도인 경우
					if (playSignalDto.getPrimaryResult() == null && playSignalDto.getSecondaryResult() == null && playSignalDto.getFinalResult() == null) {
						result.setTryCount(1);
						result.setPlaySignalSeq(unResolvedSignalDto.getPlaySignalSeq());
						result.setSenderSelected(playSignalDto.getSenderPrimarySelected());
						result.setRecipientSelected(playSignalDto.getRecipientPrimarySelected());
					}
					// 두번째 시도인 경우
					else if (playSignalDto.getPrimaryResult() != null && playSignalDto.getSecondaryResult() == null && playSignalDto.getFinalResult() == null) {
						result.setTryCount(2);
						result.setPlaySignalSeq(unResolvedSignalDto.getPlaySignalSeq());
						result.setSenderSelected(playSignalDto.getSenderPrimarySelected());
						result.setRecipientSelected(playSignalDto.getRecipientPrimarySelected());
					} else if (playSignalDto.getPrimaryResult() != null && playSignalDto.getSecondaryResult() != null && playSignalDto.getFinalResult() == null) {
						result.setTryCount(3);
						result.setPlaySignalSeq(unResolvedSignalDto.getPlaySignalSeq());
						result.setSenderSelected(playSignalDto.getSenderSecondarySelected());
						result.setRecipientSelected(playSignalDto.getRecipientSecondarySelected());
					}
					break;
			}
		}

		return result;
	}

	@Override
	@Transactional
	public boolean deleteUnResolvedSignal(MemberDto memberDto) {
		try {
			eatSignalMapper.deleteUnResolvedEatSignal(memberDto);
			playSignalMapper.deleteUnResolvedPlaySignal(memberDto);
			dailySignalMapper.deleteUnResolvedTodaySignal(memberDto);
			return true;
		} catch (Exception e) {
			log.error(String.valueOf(e));
			return false;
		}
	}

	@Override
	public List<NotificationDto> getNotification(String username) {
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(username);

		List<NotificationDto> notificationDtoList = memberMapper.getNotification(memberDao.getMemberSeq());

		return notificationDtoList;
	}

	@Override
	public boolean setNotification(NotificationDto notificationDto) {
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(notificationDto.getUsername());

		NotificationDto setData = new NotificationDto();

		setData.setMemberSeq(memberDao.getCoupleMemberSeq());
		setData.setCoupleMemberSeq(memberDao.getMemberSeq());
		if (notificationDto.getType().equals(NotificationType.SIGNAL)) {
			setData.setType(NotificationType.SIGNAL);
			setData.setMessage(NotificationMessage.SIGNAL.getText());
		} else if (notificationDto.getType().equals(NotificationType.CALENDAR)) {
			setData.setType(NotificationType.CALENDAR);
			String calendarDt = "";
			String year = notificationDto.getCalendarDt().substring(0, 4) + "년 ";
			String month = notificationDto.getCalendarDt().substring(5, 7) + "월 ";
			String day = notificationDto.getCalendarDt().substring(8, 10) + "일";

			setData.setMessage(year + month + day + "에 " + NotificationMessage.CALENDAR.getText());
		} else {
			setData.setType(NotificationType.COMMON);
			setData.setMessage(notificationDto.getMessage());
		}
		return memberMapper.setNotification(setData);
	}

	@Override
	public Map<String, String> checkedCoupleRegDt(String username) {
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(username);
		Map<String, String> result = new HashMap<>();
		if (memberDao.getCoupleRegDt() == null) {
			result.put("checkRegDt", "false");
			result.put("coupleRegDt", "null");
		} else {
			result.put("checkRegDt", "true");
			result.put("coupleRegDt", memberDao.getCoupleRegDt());
		}
		return result;
	}

	@Override
	public boolean updateCoupleRegDt(MemberDao memberDao) {
		MemberDao myInfo = memberMapper.getMemberInfoByUsername(memberDao.getUsername());
		if (myInfo.getCoupleMemberSeq() != 0L) {
			// 내 coupleRegDt 변경
			MyProfileInfo myProfileInfo = new MyProfileInfo();
			myProfileInfo.setMemberSeq(myInfo.getMemberSeq());
			myProfileInfo.setCoupleRegDt(memberDao.getCoupleRegDt());

			MyProfileInfo coupleProfileInfo = new MyProfileInfo();
			coupleProfileInfo.setMemberSeq(myInfo.getCoupleMemberSeq());
			coupleProfileInfo.setCoupleRegDt(memberDao.getCoupleRegDt());
			return memberMapper.updateCoupleRegDt(coupleProfileInfo) && memberMapper.updateCoupleRegDt(myProfileInfo);
		} else {
			// 내 coupleRegDt 변경
			MyProfileInfo myProfileInfo = new MyProfileInfo();
			myProfileInfo.setMemberSeq(myInfo.getMemberSeq());
			myProfileInfo.setCoupleRegDt(memberDao.getCoupleRegDt());
			return memberMapper.updateCoupleRegDt(myProfileInfo);
		}
	}

	@Override
	public boolean checkedCoupleCode(String coupleCode) {
		// hasCode > 0 라면 이미 존재하는 코드임
		int hasCode = memberMapper.hasCode(coupleCode);

		// hasCode == 1 이면 이미 생성한 1명만 사용하고 있는 것이기 때문에 유효함
		if (hasCode == 1) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Map<String, String> checkedCouple(String username, String coupleCode) {
		String result = "result";
		String message = "message";

		// hasCode > 0 라면 이미 존재하는 코드임
		int hasCode = memberMapper.hasCode(coupleCode);
		MemberDao myInfo = memberMapper.getMemberInfoByUsername(username);
		Map<String, String> resultMap = new HashMap<>();

		if ((hasCode == 2 || hasCode == 1) && myInfo.getCoupleCode() != null && myInfo.getCoupleCode().equals(coupleCode)) {
			resultMap.put(result, ResultCode.FALSE.getText());
			resultMap.put(message, ResultCode.ERROR_302.getText());
		} else if (hasCode == 2 && (!myInfo.getCoupleCode().equals(coupleCode) || myInfo.getCoupleCode() == null)) {
			resultMap.put(result, ResultCode.FALSE.getText());
			resultMap.put(message, ResultCode.ERROR_301.getText());
		} else if (hasCode == 1 && myInfo.getCoupleCode() != null) {
			resultMap.put(result, ResultCode.CHOICE.getText());
			resultMap.put(message, ResultCode.CHOICE_304.getText());
		} else if (hasCode == 1) {
			resultMap.put(result, ResultCode.TRUE.getText());
			resultMap.put(message, ResultCode.SUCCESS_300.getText());
		} else if (hasCode == 0) {
			resultMap.put(result, ResultCode.FALSE.getText());
			resultMap.put(message, ResultCode.ERROR_303.getText());
		} else {
			resultMap.put(result, ResultCode.FALSE.getText());
			resultMap.put(message, ResultCode.ERROR_999.getText());
		}
		return resultMap;
	}

	@Override
	public CoupleCodeDto createCode(CoupleCodeDto coupleCodeDto) throws Exception {
		int size = 4;

		String result = getRandomCode(size);

		// hasCode > 0 라면 이미 존재하는 코드임
		int hasCode = memberMapper.hasCode(result);

		MemberDao memberDao = memberMapper.getMemberInfoByUsername(coupleCodeDto.getUsername());

		if (memberDao == null) {
			coupleCodeDto.setCoupleCode(result);
			coupleCodeDto.setMessage("복사 후 완료 버튼을 누르고 \n상대방에게 전달해 주세요.");
		} else {
			coupleCodeDto.setMemberSeq(memberDao.getMemberSeq());

			if (memberDao.getCoupleCode() != null && memberDao.getCoupleMemberSeq() != null) {
				coupleCodeDto.setCoupleCode(memberDao.getCoupleCode());
				coupleCodeDto.setMessage("이미 커플 등록이 완료된 상태에요.");
				return coupleCodeDto;
			} else if (memberDao.getCoupleCode() != null && memberDao.getCoupleMemberSeq() == null) {
				coupleCodeDto.setCoupleCode(memberDao.getCoupleCode());
				coupleCodeDto.setMessage("해당 코드를 상대방에게 전달해주세요.");
				return coupleCodeDto;
			} else {
				if (hasCode > 0) {
					// 이미 존재하는 코드면 한번 더 시도
					result = getRandomCode(size);
					hasCode = memberMapper.hasCode(result);

					if (hasCode > 0) {
						// 이미 존재하는 코드면 사이즈를 늘려서 한번 더 시도
						result = getRandomCode(size + 1);
						hasCode = memberMapper.hasCode(result);
						int whileSize = size + 1;

						while (hasCode > 0) {
							whileSize++;
							result = getRandomCode(whileSize);
							hasCode = memberMapper.hasCode(result);

							if (hasCode == 0) {
								coupleCodeDto.setMessage("해당 코드를 상대방에게 전달해주세요.");
								break;
							}
						}
					} else {
						coupleCodeDto.setMessage("해당 코드를 상대방에게 전달해주세요.");
					}
				} else {
					coupleCodeDto.setMessage("해당 코드를 상대방에게 전달해주세요.");
				}
			}
			coupleCodeDto.setCoupleCode(result);
			memberMapper.setCoupleCode(coupleCodeDto);
		}
		return coupleCodeDto;
	}

	@Override
	public boolean registrationCouple(CoupleCodeDto coupleCodeDto) {
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(coupleCodeDto.getUsername());
		MemberDao coupleMemberDao = memberMapper.getMemberInfoByCoupleCode(coupleCodeDto.getCoupleCode());

		DisconnectCoupleDto getDisconnectCouple = memberMapper.getDisconnectCouple(memberDao.getMemberSeq(), coupleMemberDao.getMemberSeq());

		if (getDisconnectCouple != null) {
			// null 이 아니면 기존 데이터들 다 복구하고 coupleCode 를 예전 coupleCode 로 바꿔줘야함
			coupleCodeDto.setCoupleCode(getDisconnectCouple.getCoupleCode());
			coupleCodeDto.setCoupleRegDt(getDisconnectCouple.getCoupleRegDt());
			boolean calendarResult = memberMapper.restoreCalendar(coupleCodeDto.getCoupleCode());
			boolean anniversaryResult = memberMapper.restoreAnniversary(coupleCodeDto.getUsername());
			boolean notificationResult = memberMapper.restoreNotification(memberDao.getMemberSeq());
			boolean coupleNotificationResult = memberMapper.restoreNotification(coupleMemberDao.getMemberSeq());

			boolean eatSignalResult = signalMapper.restoreEatSignal(coupleCodeDto.getCoupleCode());
			boolean playSignalResult = signalMapper.restorePlaySignal(coupleCodeDto.getCoupleCode());

			boolean diaryResult = coupleDiaryMapper.restoreCoupleDiary(coupleCodeDto.getCoupleCode());

			boolean menstrualCycleCalendarResult = calendarMapper.restoreMenstrualCycleCalendar(coupleCodeDto.getCoupleCode());
			boolean menstrualCycleMessageCalendarResult = calendarMapper.restoreMenstrualCycleMessageCalendar(coupleCodeDto.getCoupleCode());

			coupleCodeDto.setMemberSeq(memberDao.getMemberSeq());
			coupleCodeDto.setCoupleMemberSeq(coupleMemberDao.getMemberSeq());
			coupleCodeDto.setCoupleDeviceToken(coupleMemberDao.getMyDeviceToken());
			coupleCodeDto.setCoupleNickName(coupleMemberDao.getNickName());

			// 상대편도 업데이트
			boolean coupleUpdateResult = memberMapper.updateCoupleInfo(memberDao.getMemberSeq(), memberDao.getNickName(), getDisconnectCouple.getCoupleCode(), memberDao.getMyDeviceToken(), coupleCodeDto.getCoupleRegDt(), coupleMemberDao.getUsername());

			if (coupleUpdateResult) {
				// 복구 했으면 해제했던 내용 지워줘야함
				memberMapper.deleteDisconnectCouple(getDisconnectCouple.getDisconnectSeq());
				return memberMapper.coupleRegistration(coupleCodeDto);
			} else {
				return false;
			}
		} else {
			coupleCodeDto.setMemberSeq(memberDao.getMemberSeq());
			coupleCodeDto.setCoupleMemberSeq(coupleMemberDao.getMemberSeq());
			coupleCodeDto.setCoupleDeviceToken(coupleMemberDao.getMyDeviceToken());
			coupleCodeDto.setCoupleNickName(coupleMemberDao.getNickName());
			coupleCodeDto.setCoupleRegDt(null);

			// 상대편도 업데이트
			boolean coupleUpdateResult = memberMapper.updateCoupleInfo(memberDao.getMemberSeq(), memberDao.getNickName(),coupleCodeDto.getCoupleCode(), memberDao.getMyDeviceToken(), coupleCodeDto.getCoupleRegDt(), coupleMemberDao.getUsername());

			if (coupleUpdateResult) {
				return memberMapper.coupleRegistration(coupleCodeDto);
			} else {
				return false;
			}
		}
	}

	@Override
	@Transactional
	public boolean disconnectCouple(CoupleCodeDto coupleCodeDto) {
		try {
			MemberDao memberDao = memberMapper.getMemberInfoByUsername(coupleCodeDto.getUsername());
			DisconnectCoupleDto disconnectCoupleDto = new DisconnectCoupleDto();
			disconnectCoupleDto.setMemberSeq1(memberDao.getMemberSeq());
			disconnectCoupleDto.setMemberSeq2(memberDao.getCoupleMemberSeq());
			disconnectCoupleDto.setCoupleCode(memberDao.getCoupleCode());
			disconnectCoupleDto.setCoupleRegDt(memberDao.getCoupleRegDt());

			// disconnect_couple table insert
			boolean insertDisconnectCouple = memberMapper.insertDisconnectCouple(disconnectCoupleDto);

			// 본인 연동 해제
			boolean myResult = memberMapper.coupleDisconnect(memberDao.getMemberSeq());
			// 상대편 연동 해제
			boolean opponentResult = memberMapper.coupleDisconnect(memberDao.getCoupleMemberSeq());

			// 삭제해야할 테이블 : calendar, anniversary, notification(본인,상대편), eat_signal, play_signal, couple_diary, menstrual_cycle_calendar
			boolean calendarResult = memberMapper.deleteCalendarByCoupleCode(memberDao.getCoupleCode());
			boolean anniversaryResult = memberMapper.deleteAnniversary(memberDao.getUsername());
			boolean notificationMyResult = memberMapper.deleteNotification(memberDao.getMemberSeq());
			boolean notificationCoupleResult = memberMapper.deleteNotification(memberDao.getCoupleMemberSeq());

			boolean eatSignalResult = signalMapper.deleteEatSignal(memberDao.getCoupleCode());
			boolean playSignalResult = signalMapper.deletePlaySignal(memberDao.getCoupleCode());

			boolean coupleDiaryResult = coupleDiaryMapper.deleteCoupleDiaryByDisconnectCouple(memberDao.getCoupleCode());

			boolean menstrualCycleResult = calendarMapper.deleteMenstrualCycleByCoupleCode(memberDao.getCoupleCode());
			boolean menstrualCycleMessageResult = calendarMapper.deleteMenstrualCycleMessageByCoupleCode(memberDao.getCoupleCode());

			return true;
		} catch (Exception e) {
			log.error(String.valueOf(e));
			return false;
		}
	}

	@Override
	public MemberDao getCoupleInfoByUsername(String username) {
		return memberMapper.getCoupleInfoByUsername(username);
	}

	@Override
	@Transactional
	public boolean deleteAccount(DeleteAccountDto deleteAccountDto) {
		try {
			CoupleCodeDto coupleCodeDto = new CoupleCodeDto();
			coupleCodeDto.setUsername(deleteAccountDto.getUsername());
			MemberDao myInfo = memberMapper.getMemberInfoByUsername(deleteAccountDto.getUsername());
			// 커플인 경우 커플 해제 및 데이터 validYN N으로 변경
			if (myInfo.getCoupleMemberSeq() != null) {
				boolean disconnectCoupleResult = disconnectCouple(coupleCodeDto);
			}
			// 탈퇴 사유 저장
			boolean insertDeleteAccountResult = memberMapper.insertDeleteAccount(deleteAccountDto);
			deleteAccountDto.setStatus(MemberStatus.WITHDRAWAL);

			// 멤버 상태 변경
			return memberMapper.updateStatus(deleteAccountDto);
		} catch (Exception e) {
			log.error(String.valueOf(e));
			return false;
		}
	}

	@Override
	public boolean restoreAccount(String username) {
		// 여기서 복구 기능 만들어줘야함

		DeleteAccountDto deleteAccountDto = new DeleteAccountDto();
		deleteAccountDto.setUsername(username);
		deleteAccountDto.setStatus(MemberStatus.ACTIVE);
		return memberMapper.updateStatus(deleteAccountDto);
	}

	@Override
	public boolean setAnniversary(AnniversaryDto anniversaryDto) {
		return memberMapper.setAnniversary(anniversaryDto);
	}

	/**
	 * DDayService 로 분리
	 */
	@Override
	public List<AnniversaryDto> getAnniversaryByUsername(String username) throws Exception {
		return dDayService.getAnniversary(username);
	}

	@Override
	public boolean updateAnniversary(AnniversaryDto anniversaryDto) {
		return memberMapper.updateAnniversary(anniversaryDto);
	}

	@Override
	public boolean deleteAnniversary(AnniversaryDto anniversaryDto) {
		return memberMapper.deleteAnniversaryByUser(anniversaryDto);
	}

	@Override
	public ExpressionDto getExpression(String username) {
		MemberDao myMemberDao = memberMapper.getMemberInfoByUsername(username);
		MemberDao coupleMemberDao = memberMapper.getMemberInfoBySenderMemberSeq(myMemberDao.getCoupleMemberSeq());

		ExpressionDto expressionDto = new ExpressionDto();
		expressionDto.setMyExpression(myMemberDao.getExpression());
		if (coupleMemberDao == null) {
			expressionDto.setCoupleExpression("null");
		} else {
			expressionDto.setCoupleExpression(coupleMemberDao.getExpression());
		}
		return expressionDto;
	}

	@Override
	public boolean setExpression(ExpressionDto expressionDto) throws Exception {
		try {
			boolean myResult = memberMapper.setMyExpression(expressionDto);

			MemberDao memberDao = memberMapper.getMemberInfoByUsername(expressionDto.getUsername());

			NotificationDto notificationDto = new NotificationDto();
			notificationDto.setUsername(expressionDto.getUsername());
			notificationDto.setMemberSeq(memberDao.getCoupleMemberSeq());
			notificationDto.setCoupleMemberSeq(memberDao.getMemberSeq());
			notificationDto.setType(NotificationType.EXPRESSION);
			notificationDto.setMessage(NotificationMessage.EXPRESSION.getText());

			boolean notificationResult = memberMapper.setNotification(notificationDto);

			if (notificationResult) {
				String titleMessage = NotificationType.EXPRESSION.toString();
				String bodyMessage = NotificationMessage.EXPRESSION.getText();

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
			}
			return myResult;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public MyProfileInfo getMyProfileInfoByUsername(String username) {
		MemberDao myProfileInfo = memberMapper.getMemberInfoByUsername(username);
		MemberDao coupleProfileInfo = memberMapper.getMemberInfoBySenderMemberSeq(myProfileInfo.getCoupleMemberSeq());

		MyProfileInfo result = new MyProfileInfo();

		result.setNickName(myProfileInfo.getNickName());
		if (myProfileInfo.getCoupleNickName() == null) {
			result.setCoupleNickName("null");
		} else {
			result.setCoupleNickName(myProfileInfo.getCoupleNickName());
		}

		if (myProfileInfo.getMyProfileImgAddr() == null) {
			result.setMyProfileImgAddr("null");
		} else {
			result.setMyProfileImgAddr(myProfileInfo.getMyProfileImgAddr());
		}

		if (coupleProfileInfo == null) {
			result.setCoupleProfileImgAddr("null");
			result.setCoupleExpression("null");
		} else {
			if (coupleProfileInfo.getMyProfileImgAddr() == null) {
				result.setCoupleProfileImgAddr("null");
			} else {
				result.setCoupleProfileImgAddr(coupleProfileInfo.getMyProfileImgAddr());
			}
			if (coupleProfileInfo.getExpression() == null || Objects.equals(coupleProfileInfo.getExpression(), "") || Objects.equals(coupleProfileInfo.getExpression(), "null")) {
				result.setCoupleExpression("null");
			} else {
				result.setCoupleExpression(coupleProfileInfo.getExpression());
			}
		}

		if (myProfileInfo.getMainBannerImgAddr() == null) {
			result.setMainBannerImgAddr("null");
		} else {
			result.setMainBannerImgAddr(myProfileInfo.getMainBannerImgAddr());
		}

		if (myProfileInfo.getCoupleRegDt() == null) {
			result.setCoupleRegDt("null");
		} else {
			LocalDateTime coupleRegDtDateTime = LocalDateTime.parse(myProfileInfo.getCoupleRegDt(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			result.setCoupleRegDt(coupleRegDtDateTime.toString());
		}

		if (myProfileInfo.getExpression() == null || Objects.equals(myProfileInfo.getExpression(), "") || Objects.equals(myProfileInfo.getExpression(), "null")) {
			result.setMyExpression("null");
		} else {
			result.setMyExpression(myProfileInfo.getExpression());
		}
		return result;
	}

	@Override
	public boolean updateNickName(MyProfileInfo myProfileInfo) throws Exception {
		try {
			if (myProfileInfo.getNickName() != null) {
				MemberDao memberDao = memberMapper.getMemberInfoByUsername(myProfileInfo.getUsername());
				MemberDao coupleMemberDao = memberMapper.getMemberInfoBySenderMemberSeq(memberDao.getCoupleMemberSeq());

				memberMapper.updateCoupleInfo(coupleMemberDao.getCoupleMemberSeq(), myProfileInfo.getNickName(), coupleMemberDao.getCoupleCode(), coupleMemberDao.getCoupleDeviceToken(), coupleMemberDao.getCoupleRegDt(), coupleMemberDao.getUsername());
				return memberMapper.updateNickName(myProfileInfo);
			} else {
				log.error("닉네임 업데이트 에러");
				throw new Exception("닉네임 업데이트 에러");
			}
		} catch (Exception e) {
			log.error("닉네임 업데이트 에러", e);
			throw new Exception(e);
		}
	}

	@Override
	public boolean updateMyProfileImgAddr(MyProfileInfo myProfileInfo) {
		return memberMapper.setMyProfileImg(myProfileInfo);
	}

	@Override
	public boolean updateMainBannerImgAddr(MyProfileInfo myProfileInfo) {
		return memberMapper.setMainBannerImg(myProfileInfo);
	}

	@Override
	public List<RecentSignalDto> getRecentSignalByUsername(String username) {
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(username);
		List<RecentSignalDto> eatRecentSignalDto = signalMapper.getEatRecentSignal(memberDao);
		List<RecentSignalDto> playRecentSignalDto = signalMapper.getPlayRecentSignal(memberDao);

		List<RecentSignalDto> recentSignalDto = new ArrayList<>();
		List<RecentSignalDto> result = new ArrayList<>(20);

		recentSignalDto.addAll(eatRecentSignalDto);
		recentSignalDto.addAll(playRecentSignalDto);

		DateComparator dc = new DateComparator();
		// 오름차순
		Collections.sort(recentSignalDto, dc);
//		for (RecentSignalDto r : recentSignalDto)
//			System.out.println(r.getCategory() + ", " + r.getRegDt());
		// 내림차순
		Collections.reverse(recentSignalDto);
//		for (RecentSignalDto r : recentSignalDto) {
//			System.out.println(r.getCategory() + ", " + r.getRegDt());
//			System.out.println(r.getFinalResult());
//		}

		if (recentSignalDto.size() == 0) {
			result = null;
		} else if (recentSignalDto.size() < 20) {
			result = recentSignalDto.subList(0, recentSignalDto.size());
		} else {
			result = recentSignalDto.subList(0, 20);
		}
		return result;
	}

	@Override
	public List<RecentSignalDto> getAllSignalListByMemberSeq(Long memberSeq, String category) {
		MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(memberSeq);

		List<RecentSignalDto> result = new ArrayList<>();

		if (Objects.equals(category, "eatSignal")) {
			result = signalMapper.getAllEatSignalList(memberDao);
		}

		if (Objects.equals(category, "playSignal")) {
			result = signalMapper.getAllPlaySignalList(memberDao);
		}

		return result;
	}

	@Override
	public boolean getAppVersion(String platform, String version) {
		if (platform.equals("iOS")) {
			List<String> result = memberMapper.getAppVersionIos();
			List<Integer> toIntResult = new ArrayList<>();

			int currentVersion = Integer.parseInt(version.replaceAll("\\.", ""));

			for (String s : result) {
				toIntResult.add(Integer.parseInt(s.replaceAll("\\.", "")));
			}

			return toIntResult.contains(currentVersion);
		} else {
			List<String> result = memberMapper.getAppVersionAndroid();
			List<Integer> toIntResult = new ArrayList<>();

			int currentVersion = Integer.parseInt(version.replaceAll("\\.", ""));

			for (String s : result) {
				toIntResult.add(Integer.parseInt(s.replaceAll("\\.", "")));
			}

			return toIntResult.contains(currentVersion);
		}
	}

	@Override
	public MostMatchedSignalItemDto getMostMatchedSignalItem(MostMatchedSignalItemDto mostMatchedSignalItemDto) {
		if (Objects.equals(mostMatchedSignalItemDto.getCategory(), "eatSignal")) {

			MostMatchedSignalItemDto mostMatchedSignalItemDto1 = signalMapper.getMostMatchedEatSignalItem(mostMatchedSignalItemDto);

			// null 처리
			if (mostMatchedSignalItemDto1 == null || mostMatchedSignalItemDto1.getMostMatchedSignalItem() == null) {
				MostMatchedSignalItemDto mostMatchedSignalItemDto2 = new MostMatchedSignalItemDto();
				mostMatchedSignalItemDto2.setMostMatchedSignalItem("null");
				return mostMatchedSignalItemDto2;
			}

			return mostMatchedSignalItemDto1;
		} else {
			MostMatchedSignalItemDto mostMatchedSignalItemDto1 = signalMapper.getMostMatchedPlaySignalItem(mostMatchedSignalItemDto);

			// null 처리
			if (mostMatchedSignalItemDto1 == null || mostMatchedSignalItemDto1.getMostMatchedSignalItem() == null) {
				MostMatchedSignalItemDto mostMatchedSignalItemDto2 = new MostMatchedSignalItemDto();
				mostMatchedSignalItemDto2.setMostMatchedSignalItem("null");
				return mostMatchedSignalItemDto2;
			}

			return mostMatchedSignalItemDto1;
		}
	}

	@Override
	public boolean setInquiry(InquiryDto inquiryDto) throws IOException {
		// 띄어쓰기, 줄바꿈 처리해야함
		String result = inquiryDto.getInquiries().replaceAll("\\p{Z}", "[]");
		String wordBreak = result.replaceAll("(\r\n|\r|\n|\n\r)", "{}");
		inquiryDto.setInquiries(result);

		boolean inquiryResult = memberMapper.setInquiry(inquiryDto);

		if (inquiryResult) {
			String titleMessage = NotificationType.HEARTNAL.toString();
			String bodyMessage = "MemberSeq = " + inquiryDto.getMemberSeq().toString() + " 의 1:1문의가 등록되었어요.";

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

			MemberDao daehee = memberMapper.getMemberInfoBySenderMemberSeq(77L);
			cloudMessagingService.sendMessageTo(daehee.getMyDeviceToken(), titleMessage, bodyMessage, fcmData, androidConfig, apnsConfig);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<InquiryDto> getInquiry(Long memberSeq) {
		// 띄어쓰기, 줄바꿈 처리해야함
		List<InquiryDto> result = memberMapper.getInquiry(memberSeq);

		for (InquiryDto inquiryDto : result) {
			String spaceChange = inquiryDto.getInquiries().replaceAll("\\[\\]", " ");
			String wordBreak = spaceChange.replaceAll("\\{\\}", System.lineSeparator());
			inquiryDto.setInquiries(wordBreak);
		}

		for (InquiryDto inquiryDto : result) {
			if (inquiryDto.getAnswerContent() != null) {
				String spaceChange = inquiryDto.getAnswerContent().replaceAll("\\[\\]", " ");
				String wordBreak = spaceChange.replaceAll("\\{\\}", System.lineSeparator());
				inquiryDto.setAnswerContent(wordBreak);
			}
		}

		return result;
	}

	static class DateComparator implements Comparator<RecentSignalDto> {
		@Override
		public int compare(RecentSignalDto r1, RecentSignalDto r2) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

			Date day1 = null;
			Date day2 = null;

			try {
				day1 = format.parse(r1.getRegDt());
				day2 = format.parse(r2.getRegDt());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			int result = day1.compareTo(day2);
			return result;
		}
	}

	// 랜덤 문자 난수 생성 함수
	public static String getRandomCode(int size) throws Exception {
		if (size > 0) {
			char[] tmp = new char[size];
			for (int i = 0; i < tmp.length; i++) {
				int div = (int) Math.floor(Math.random() * 2);

				if (div == 0) {
					tmp[i] = (char) (Math.random() * 10 + '0');
				} else {
					tmp[i] = (char) (Math.random() * 26 + 'A');
				}
			}
			return new String(tmp);
		} else {
			throw new Exception("랜덤 문자 난수 생성 오류");
		}
	}
}
