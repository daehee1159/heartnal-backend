package com.msm.heartnal.api.controller;

import com.msm.heartnal.api.service.MemberService;
import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.*;
import com.msm.heartnal.core.dto.inquiry.InquiryDto;
import com.msm.heartnal.core.enums.MemberStatus;
import com.msm.heartnal.core.jwt.JwtComponent;
import com.msm.heartnal.core.jwt.service.JwtService;
import com.msm.heartnal.core.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 최대희
 * @since 2021-06-17
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/member", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;
	private final MemberMapper memberMapper;

	private final JwtComponent jwtComponent;

	private final JwtService jwtService;

	@Value("/get_access_token")
	private String accessTokenUrl;

	/**
	 * 회원 가입 체크
	 */
	@RequestMapping(value = "/registration/check/{email}", method = RequestMethod.GET)
	public MemberStatus memberRegistrationCheck(@PathVariable String email) {
		return memberService.memberRegistrationCheck(email);
	}

	/**
	 * 회원정보
	 * 스케줄러 때문에 만듬
	 */
	@RequestMapping(value = "/info/{memberSeq}", method = RequestMethod.GET)
	public MemberDao getMemberInfo(@PathVariable Long memberSeq) {
		return memberMapper.getMemberInfoBySenderMemberSeq(memberSeq);
	}

	/**
	 * email 을 넘기면 memberSeq 를 리턴
	 */
	@RequestMapping(value = "/{email}", method = RequestMethod.GET)
	public Long getMemberSeqByEmail(@PathVariable String email) {
		return memberService.getMemberSeqByEmail(email);
	}

	/**
	 * memberSeq 를 넘기면 coupleCode 를 리턴
	 */
	@RequestMapping(value = "/code/{memberSeq}", method = RequestMethod.GET)
	public String getCoupleCodeByMemberSeq(@PathVariable Long memberSeq) {
		return memberService.getCoupleCodeByMemberSeq(memberSeq);
	}

	/**
	 * 회원가입
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public Object memberRegistration(@RequestBody MemberDto memberDto) {
		return memberService.memberRegistration(memberDto);
	}

	/**
	 * iOS 회원일 경우 identifier + email 저장
	 */
	@RequestMapping(value = "/ios", method = RequestMethod.POST)
	public Object iosMemberRegistration(@RequestBody IosMemberDto iosMemberDto) {
		return memberService.iosMemberRegistration(iosMemberDto);
	}

	/**
	 * Get iOS Member
	 */
	@RequestMapping(value = "/ios/{identifier}", method = RequestMethod.GET)
	public String getIosMember(@PathVariable String identifier) {
		return memberService.getIosMember(identifier);
	}

	/**
	 * Get FCM Token
	 */
	@RequestMapping(value = "/check/token/{memberSeq}", method = RequestMethod.GET)
	public String getFCMToken(@PathVariable Long memberSeq) {
		return memberService.getFCMToken(memberSeq);
	}

	/**
	 * 커플 연동이 되어 있는지 확인
	 */
	@RequestMapping(value = "/check/couple/connect/{username}", method = RequestMethod.GET)
	public boolean isCheckCoupleConnect(@PathVariable String username) {
		return memberService.isCheckCoupleConnect(username);
	}

	/**
	 * 기기 변경으로 인한 Device Token 변경
	 */
	@RequestMapping(value = "/change/device", method = RequestMethod.POST)
	public boolean changedDeviceToken(@RequestBody MemberDto memberDto) {
		return memberService.changedDeviceToken(memberDto);
	}

	/**
	 * 오류상황으로 인한 초기화가 필요할 때
	 */
	@RequestMapping(value = "/initialization", method = RequestMethod.POST)
	public Object memberInitialization(@RequestBody DeleteAccountDto deleteAccountDto) {
		return memberService.memberInitialization(deleteAccountDto);
	}

	/**
	 * 내 프로필 이미지 업로드
	 */
	@RequestMapping(value = "/update/profile/img", method = RequestMethod.POST)
	public boolean updateMyProfileImgAddr(@RequestBody MyProfileInfo myProfileInfo) {
		return memberService.updateMyProfileImgAddr(myProfileInfo);
	}

	/**
	 * 메인 배너 이미지 업로드
	 */
	@RequestMapping(value = "/update/banner/img", method = RequestMethod.POST)
	public boolean updateMainBannerImgAddr(@RequestBody MyProfileInfo myProfileInfo) {
		return memberService.updateMainBannerImgAddr(myProfileInfo);
	}

	/**
	 * Set Calendar
	 */
	@RequestMapping(value = "/calendar", method = RequestMethod.POST)
	public boolean setCalendar(@RequestBody CalendarRequestDto calendarRequestDto) throws IOException  {
		return memberService.setCalendar(calendarRequestDto);
	}

	/**
	 * Get Calendar
	 */
	@RequestMapping(value = "/calendar/{username}", method = RequestMethod.GET)
	public List<CalendarResponseDto> getCalendar(@PathVariable String username) {
		return memberService.getCalendar(username);
	}

	/**
	 * Delete Calendar
	 */
	@RequestMapping(value = "/delete/calendar", method = RequestMethod.POST)
	public boolean deleteCalendar(@RequestBody CalendarRequestDto calendarRequestDto) throws IOException  {
		return memberService.deleteCalendar(calendarRequestDto);
	}

	/**
	 * 미확인 시그널 체크
	 */
	@RequestMapping(value = "/signal/turn/{category}/{signalSeq}/{memberSeq}", method = RequestMethod.GET)
	public CheckMyTurnDto checkMyTurn(@PathVariable String category, @PathVariable Long signalSeq, @PathVariable Long memberSeq) {
		return memberService.checkMyTurn(category, signalSeq, memberSeq);
	}

	/**
	 * 미확인 시그널 체크
	 */
	@RequestMapping(value = "/temp/signal/{memberSeq}", method = RequestMethod.GET)
	public List<TempSignalDto> getTempSignal(@PathVariable Long memberSeq) {
		return memberService.getTempSignal(memberSeq);
	}

	/**
	 * 미확인 시그널 삭제
	 */
	@RequestMapping(value = "/delete/temp/signal", method = RequestMethod.POST)
	public boolean deleteTempSignal(@RequestBody TempSignalDto tempSignalDto) {
		return memberService.deleteTempSignal(tempSignalDto);
	}

	/**
	 * 미완료 시그널 체크 by coupleCode
	 * 시그널 현황을 보여주기 위해 커플 코드를 기준으로 데이터를 반환할 필요가 있음
	 */
	@RequestMapping(value = "/check/unresolved/{coupleCode}/{memberSeq}", method = RequestMethod.GET)
	public CoupleUnResolvedSignalDto coupleUnResolvedSignal(@PathVariable String coupleCode, @PathVariable Long memberSeq) {
		return memberService.coupleUnResolvedSignal(coupleCode, memberSeq);
	}

	/**
	 * 미완료 시그널 체크
	 */
	@RequestMapping(value = "/check/signal/{username}", method = RequestMethod.GET)
	public HasUnResolvedSignal hasUnResolvedSignal(@PathVariable String username) {
		return memberService.hasUnResolvedSignal(username);
	}

	@RequestMapping(value = "/check/signal", method = RequestMethod.POST)
	public UnResolvedSignalDto setUnResolvedSignal(@RequestBody UnResolvedSignalDto unResolvedSignalDto) {
		return memberService.setUnResolvedSignal(unResolvedSignalDto);
	}

	/**
	 * 미완료 시그널 삭제
	 */
	@RequestMapping(value = "/delete/unresolved/signal", method = RequestMethod.POST)
	public boolean deleteUnResolvedSignal(@RequestBody MemberDto memberDto) {
		return memberService.deleteUnResolvedSignal(memberDto);
	}

	/**
	 * 알림 정보 불러오기
	 */
	@RequestMapping(value = "/notification/{username}", method = RequestMethod.GET)
	public List<NotificationDto> getNotification(@PathVariable String username) {
		return memberService.getNotification(username);
	}

	/**
	 * Checked CoupleRegDt
	 */
	@RequestMapping(value = "/couple/reg-dt/{username}", method = RequestMethod.GET)
	public Map<String, String> checkedCoupleRegDt(@PathVariable String username) {
		return memberService.checkedCoupleRegDt(username);
	}

	/**
	 * Update Couple D-Day
	 */
	@RequestMapping(value = "/couple/d-day", method = RequestMethod.POST)
	public boolean updateCoupleRegDt(@RequestBody MemberDao memberDao) {
		return memberService.updateCoupleRegDt(memberDao);
	}

	/**
	 * 커플 코드 체크
	 */
	@RequestMapping(value = "/check/code/{coupleCode}", method = RequestMethod.GET)
	public boolean createCode(@PathVariable String coupleCode) {
		return memberService.checkedCoupleCode(coupleCode);
	}

	/**
	 * 커플 코드가 등록되어 있는 커플인지 아닌지 확인
	 */
	@RequestMapping(value = "/check/couple/{username}/{coupleCode}", method = RequestMethod.GET)
	public Map<String, String> checkedCouple(@PathVariable String username, @PathVariable String coupleCode) {
		return memberService.checkedCouple(username, coupleCode);
	}

	/**
	 * 커플 코드 생성을 위한 랜덤 문자열 생성
	 */
	@RequestMapping(value = "/code", method = RequestMethod.POST)
	public CoupleCodeDto createCode(@RequestBody CoupleCodeDto coupleCodeDto) throws Exception {
		return memberService.createCode(coupleCodeDto);
	}

	/**
	 * 커플 연동
	 */
	@RequestMapping(value = "/connect/couple", method = RequestMethod.POST)
	public boolean connectCouple(@RequestBody CoupleCodeDto coupleCodeDto) {
		return memberService.registrationCouple(coupleCodeDto);
	}

	/**
	 * 커플 해제
	 */
	@RequestMapping(value = "/disconnect/couple", method = RequestMethod.POST)
	public Object disconnectCouple(@RequestBody CoupleCodeDto coupleCodeDto) {
		return memberService.disconnectCouple(coupleCodeDto);
	}

	/**
	 * 커플 정보
	 */
	@RequestMapping(value = "/couple/info/{username}", method = RequestMethod.GET)
	public MemberDao getCoupleInfoByUsername(@PathVariable String username) {
		return memberService.getCoupleInfoByUsername(username);
	}

	/**
	 * 회원 탈퇴
	 */
	@RequestMapping(value = "/delete/account", method = RequestMethod.POST)
	public boolean deleteAccount(@RequestBody DeleteAccountDto deleteAccountDto) {
		return memberService.deleteAccount(deleteAccountDto);
	}

	/**
	 * 회원 탈퇴 복구
	 * TODO 추후 /delete/account/restore 로 바꿔줘야함
	 */
	@RequestMapping(value = "/delete/restore/{username}", method = RequestMethod.GET)
	public boolean restoreAccount(@PathVariable String username) {
		return memberService.restoreAccount(username);
	}

	/**
	 * 기념일 추가
	 */
	@RequestMapping(value = "/anniversary", method = RequestMethod.POST)
	public boolean setAnniversary(@RequestBody AnniversaryDto anniversaryDto) {
		return memberService.setAnniversary(anniversaryDto);
	}

	/**
	 * 기념일 조회
	 */
	@RequestMapping(value = "/anniversary/{username}", method = RequestMethod.GET)
	public List<AnniversaryDto> getAnniversaryByUsername(@PathVariable String username) throws Exception {
		return memberService.getAnniversaryByUsername(username);
	}

	/**
	 * 기념일 수정
	 */
	@RequestMapping(value = "/anniversary/update", method = RequestMethod.POST)
	public boolean updateAnniversary(@RequestBody AnniversaryDto anniversaryDto) throws Exception {
		return memberService.updateAnniversary(anniversaryDto);
	}

	/**
	 * 기념일 삭제
	 */
	@RequestMapping(value = "/anniversary/delete", method = RequestMethod.POST)
	public boolean deleteAnniversary(@RequestBody AnniversaryDto anniversaryDto) throws Exception {
		return memberService.deleteAnniversary(anniversaryDto);
	}

	/**
	 * Get Expression
	 */
	@RequestMapping(value = "/expression/{username}", method = RequestMethod.GET)
	public ExpressionDto getExpression(@PathVariable String username) {
		return memberService.getExpression(username);
	}

	/**
	 * Set Expression
	 */
	@RequestMapping(value = "/expression", method = RequestMethod.POST)
	public boolean setExpression(@RequestBody ExpressionDto expressionDto) throws Exception {
		return memberService.setExpression(expressionDto);
	}

	/**
	 * Get MyProfileInfo
	 */
	@RequestMapping(value = "/profile/{username}", method = RequestMethod.GET)
	public MyProfileInfo getMyProfileInfoByUsername(@PathVariable String username) {
		return memberService.getMyProfileInfoByUsername(username);
	}

	/**
	 * Update NickName
	 */
	@RequestMapping(value = "/profile/nickname", method = RequestMethod.POST)
	public boolean updateMyNickName(@RequestBody MyProfileInfo myProfileInfo) throws Exception {
		return memberService.updateNickName(myProfileInfo);
	}

	/**
	 * Get RecentSignal
	 */
	@RequestMapping(value = "/signal/recent/{username}", method = RequestMethod.GET)
	public List<RecentSignalDto> getRecentSignalByUsername(@PathVariable String username) {
		return memberService.getRecentSignalByUsername(username);
	}

	/**
	 * Get AllRecentSignal
	 */
	@RequestMapping(value = "/signal/recent/all/{memberSeq}/{category}", method = RequestMethod.GET)
	public List<RecentSignalDto> getAllSignalListByMemberSeq(@PathVariable Long memberSeq, @PathVariable String category) {
		return memberService.getAllSignalListByMemberSeq(memberSeq, category);
	}

	/**
	 * Get App Version
	 */
	@RequestMapping(value = "/app/version/{platform}/{version}", method = RequestMethod.GET)
	public boolean getAppVersion(@PathVariable String platform, @PathVariable String version) {
		return memberService.getAppVersion(platform, version);
	}

	/**
	 * 가장 많이 매칭된 오늘 뭐먹지 아이템
	 */
	@RequestMapping(value = "/most/matched/item", method = RequestMethod.POST)
	public MostMatchedSignalItemDto getMostMatchedSignalItem(@RequestBody MostMatchedSignalItemDto mostMatchedSignalItemDto) {
		return memberService.getMostMatchedSignalItem(mostMatchedSignalItemDto);
	}

	/**
	 * Set One-on-One Inquiry
	 */
	@RequestMapping(value = "/inquiry", method = RequestMethod.POST)
	public boolean setInquiry(@RequestBody InquiryDto inquiryDto) throws Exception {
		return memberService.setInquiry(inquiryDto);
	}

	/**
	 * Get One-on-One Inquiry
	 */
	@RequestMapping(value = "/inquiry/{memberSeq}", method = RequestMethod.GET)
	public List<InquiryDto> getInquiry(@PathVariable Long memberSeq) {
		return memberService.getInquiry(memberSeq);
	}

	/**
	 * Flutter Error Log
	 */
	@RequestMapping(value = "/error", method = RequestMethod.POST)
	public boolean setErrorLog(@RequestBody Map<String, String> errorLog) {
		if (errorLog.get("errorLog").equals("")) {
			return false;
		} else {
			log.error(errorLog.get("errorLog"));
			return true;
		}
	}
}
