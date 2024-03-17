package com.msm.heartnal.core.mapper;

import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.*;
import com.msm.heartnal.core.dto.inquiry.InquiryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 최대희
 * @since 2021-06-17
 */
@Mapper
public interface MemberMapper {
	/* 회원가입 */
	boolean memberRegistration(MemberDao memberDao);

	/* iOS 회원 identifier + email 저장 */
	boolean iosMemberRegistration(IosMemberDto iosMemberDto);
	IosMemberDto getIosMember(String identifier);

	String getFCMToken(Long memberSeq);

	/* username 으로 조회 */
	MemberDao getMemberInfoByUsername(String username);

	/* senderMemberSeq 로 조회 */
	MemberDao getMemberInfoBySenderMemberSeq(Long senderMemberSeq);

	/* id 로 회원 조회 */
	MemberDao getMemberInfoById(String id);

	/* coupleCode 로 회원 조회 */
	MemberDao getMemberInfoByCoupleCode(String coupleCode);

	/* coupleCode 로 회원 조회 */
	boolean changedDeviceToken(@Param("username") String username, @Param("myDeviceToken") String myDeviceToken, @Param("coupleDeviceToken") String coupleDeviceToken);

	/* Calendar */
	boolean setCalendar(CalendarRequestDto calendarRequestDto);
	List<CalendarResponseDto> getCalendar(String coupleCode);
	boolean deleteCalendar(CalendarRequestDto calendarRequestDto);

	/* 미확인 시그널 */
	List<TempSignalDto> getTempSignal(Long memberSeq);
	int setTempSignal(TempSignalDto tempSignalDto);
	boolean deleteTempSignal(TempSignalDto tempSignalDto);

	/* 미답변 시그널 찾기 */
	Long coupleUnResolvedEatSignal(String coupleCode);
	Long coupleUnResolvedPlaySignal(String coupleCode);
	EatSignalDto hasUnresolvedSenderEatSignal(Long senderMemberSeq);
	PlaySignalDto hasUnresolvedSenderPlaySignal(Long senderMemberSeq);

	EatSignalDto hasUnresolvedRecipientEatSignal(Long recipientMemberSeq);
	PlaySignalDto hasUnresolvedRecipientPlaySignal(Long recipientMemberSeq);

	/* Notification Page */
	List<NotificationDto> getNotification(Long memberSeq);
	// set 할 때 memberSeq 는 상대 memberSeq, coupleMemberSeq 는 내 memberSeq 넣어야 함
	boolean setNotification(NotificationDto notificationDto);

	boolean updateCoupleRegDt(MyProfileInfo myProfileInfo);

	/* Couple Code */
	int hasCode(String coupleCode);
	boolean setCoupleCode(CoupleCodeDto coupleCodeDto);

	/* Couple Registration */
	boolean coupleRegistration(CoupleCodeDto coupleCodeDto);
	/* Get DisconnectCouple */
	DisconnectCoupleDto getDisconnectCouple(@Param("memberSeq1") Long memberSeq1, @Param("memberSeq2") Long memberSeq2);

	boolean deleteDisconnectCouple(Long disconnectSeq);

	/* Restore DisconnectCouple */
	boolean restoreCalendar(String coupleCode);
	boolean restoreAnniversary(String username);
	boolean restoreNotification(Long memberSeq);

	/* Restore DisconnectCouple */

	/* Couple Disconnect */
	boolean insertDisconnectCouple(DisconnectCoupleDto disconnectCoupleDto);
	boolean coupleDisconnect(Long memberSeq);
	/* Couple Disconnect calendar */
	boolean deleteCalendarByCoupleCode(String coupleCode);
	/* Couple Disconnect anniversary */
	boolean deleteAnniversary(String username);
	/* Couple Disconnect notification */
	boolean deleteNotification(Long memberSeq);

	/* Couple Info */
	MemberDao getCoupleInfoByUsername(String username);

	/* Delete Account */
	boolean insertDeleteAccount(DeleteAccountDto deleteAccountDto);
	boolean updateStatus(DeleteAccountDto deleteAccountDto);
	boolean deleteAccount(DeleteAccountDto deleteAccountDto);

	/* set Anniversary */
	boolean setAnniversary(AnniversaryDto anniversaryDto);
	/* get Anniversary */
	List<AnniversaryDto> getAnniversaryByUsername(String username);
	/* update Anniversary */
	boolean updateAnniversary(AnniversaryDto anniversaryDto);
	/* delete Anniversary */
	boolean deleteAnniversaryByUser(AnniversaryDto anniversaryDto);

	/* Update NickName */
	boolean updateNickName(MyProfileInfo myProfileInfo);
	/* Update CoupleNickName */
	boolean updateCoupleInfo(
		@Param("coupleMemberSeq") Long coupleMemberSeq,
		@Param("coupleNickName") String coupleNickName,
		@Param("coupleCode") String coupleCode,
		@Param("coupleDeviceToken") String coupleDeviceToken,
		@Param("coupleRegDt") String coupleRegDt,
		@Param("username") String username
	);

	/* Set Profile Img */
	boolean setMyProfileImg(MyProfileInfo myProfileInfo);
	boolean setMainBannerImg(MyProfileInfo myProfileInfo);

	/* Set Expression */
	boolean setMyExpression(ExpressionDto expressionDto);

	/* Get App Version */
	List<String> getAppVersionIos();
	List<String> getAppVersionAndroid();

	boolean setInquiry(InquiryDto inquiryDto);

	List<InquiryDto> getInquiry(Long memberSeq);
}
