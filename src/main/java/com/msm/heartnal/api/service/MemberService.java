package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.*;
import com.msm.heartnal.core.dto.inquiry.InquiryDto;
import com.msm.heartnal.core.enums.MemberStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 최대희
 * @since 2021-06-17
 */
public interface MemberService {

	// 가입 여부 체크
	MemberStatus memberRegistrationCheck(String email);

	// memberSeq 리턴
	Long getMemberSeqByEmail(String email);

	// coupleCode 리턴
	String getCoupleCodeByMemberSeq(Long memberSeq);

	// 회원 가입
	Object memberRegistration(MemberDto memberDto);

	// iOS 회원 identifier + email 저장
	boolean iosMemberRegistration(IosMemberDto iosMemberDto);

	// Get iOS Member
	String getIosMember(String identifier);

	// Get FCM Token
	String getFCMToken(Long memberSeq);

	// 커플 연동이 되어 있는지 확인
	boolean isCheckCoupleConnect(String username);

	// 기기 변경으로 인한 deviceToken 변경
	boolean changedDeviceToken(MemberDto memberDto);

	// 오류상황으로 인한 초기화
	boolean memberInitialization(DeleteAccountDto deleteAccountDto);

	MemberDao getMemberInfoByUsername(String username);

	// 권한 체크
	boolean hasAdminRole();
	boolean hasUserRole();

	// Calendar
	boolean setCalendar(CalendarRequestDto calendarRequestDto) throws IOException;
	List<CalendarResponseDto> getCalendar(String username);
	boolean deleteCalendar(CalendarRequestDto calendarRequestDto);

	// 시그널 현황
	CheckMyTurnDto checkMyTurn(String category, Long signalSeq, Long memberSeq);
	// 미확인 시그널
	List<TempSignalDto> getTempSignal(Long memberSeq);
	boolean deleteTempSignal(TempSignalDto tempSignalDto);

	// 아직 답변이 안된 시그널이 있는지
	CoupleUnResolvedSignalDto coupleUnResolvedSignal(String coupleCode, Long memberSeq);
	HasUnResolvedSignal hasUnResolvedSignal(String username);
	UnResolvedSignalDto setUnResolvedSignal(UnResolvedSignalDto unResolvedSignalDto);
	boolean deleteUnResolvedSignal(MemberDto memberDto);

	// 알림 페이지
	List<NotificationDto> getNotification(String username);
	boolean setNotification(NotificationDto notificationDto);

	// Checked CoupleRegDt
	Map<String, String> checkedCoupleRegDt(String username);

	// 커플 디데이 추가
	boolean updateCoupleRegDt(MemberDao memberDao);

	// 커플 코드 체크
	boolean checkedCoupleCode(String coupleCode);

	// 커플인지 아닌지 확인
	Map<String, String> checkedCouple(String username, String coupleCode);

	// 커플 코드 생성
	CoupleCodeDto createCode(CoupleCodeDto coupleCodeDto) throws Exception;

	// 커플 등록하기
	boolean registrationCouple(CoupleCodeDto coupleCodeDto);

	// 커플 연결 끊기
	boolean disconnectCouple(CoupleCodeDto coupleCodeDto);

	// 커플 정보 가져오기
	MemberDao getCoupleInfoByUsername(String username);

	// 회원 탈퇴
	boolean deleteAccount(DeleteAccountDto deleteAccountDto);

	// 회원 탈퇴 복구
	boolean restoreAccount(String username);

	// 기념일 추가
	boolean setAnniversary(AnniversaryDto anniversaryDto);
	// 기념일 조회
	List<AnniversaryDto> getAnniversaryByUsername(String username) throws Exception;

	boolean updateAnniversary(AnniversaryDto anniversaryDto);

	boolean deleteAnniversary(AnniversaryDto anniversaryDto);

	// 내 기분 조회
	ExpressionDto getExpression(String username);
	// Set Expression
	boolean setExpression(ExpressionDto expressionDto) throws Exception;

	// Get MyProfileInfo
	MyProfileInfo getMyProfileInfoByUsername(String username);

	// Update NickName or CoupleNickName
	boolean updateNickName(MyProfileInfo myProfileInfo) throws Exception;

	boolean updateMyProfileImgAddr(MyProfileInfo myProfileInfo);
	boolean updateMainBannerImgAddr(MyProfileInfo myProfileInfo);

	// Get Recent Signal
	List<RecentSignalDto> getRecentSignalByUsername(String username);

	List<RecentSignalDto> getAllSignalListByMemberSeq(Long memberSeq, String category);

	// Get App Version
	boolean getAppVersion(String platform, String version);

	MostMatchedSignalItemDto getMostMatchedSignalItem(MostMatchedSignalItemDto mostMatchedSignalItemDto);

	boolean setInquiry(InquiryDto inquiryDto) throws IOException;

	List<InquiryDto> getInquiry(Long memberSeq);
}
