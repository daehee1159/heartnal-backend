package com.msm.heartnal.core.dao;

import com.msm.heartnal.core.dto.MemberDto;
import com.msm.heartnal.core.enums.MemberStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * @author 최대희
 * @since 2021-06-17
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDao implements UserDetails {
	private static final long serialVersionUID = 1L;

	private Long memberSeq;
	private Long coupleMemberSeq;

	private String password;
	private String gender;
	private String coupleNickName;
	private String nickName;
	private String email;

	private String coupleCode;
	private String myDeviceToken;
	private String coupleDeviceToken;

	private String myProfileImgAddr;
	private String coupleProfileImgAddr;
	private String mainBannerImgAddr;

	private String expression;

	private MemberStatus status;
	private String userRole;

	private LocalDateTime regDt;
	private LocalDateTime modDt;
	private String coupleRegDt;

	// UserDetails 기본 상속 변수
	private Collection<? extends GrantedAuthority> authorities;
	private final boolean isEnabled = true;
	private String username;
	private final boolean isCredentialsNonExpired = true;
	private final boolean isAccountNonExpired = true;
	private final boolean isAccountNonLocked = true;

	public static MemberDao of(MemberDto memberDto, String encodedPassword) {
		// 유효성 검증
		validate(memberDto, encodedPassword);

		MemberDao memberDao = new MemberDao();

		memberDao.memberSeq = memberDto.getMemberSeq();
		memberDao.coupleMemberSeq = memberDto.getCoupleMemberSeq();

		memberDao.username = memberDto.getEmail();
		memberDao.password = encodedPassword;

		memberDao.gender = memberDto.getGender();
		memberDao.nickName = memberDto.getNickName();
		memberDao.coupleNickName = memberDto.getCoupleNickName();
		memberDao.email = memberDto.getEmail();

		memberDao.coupleCode = memberDto.getCoupleCode();
		memberDao.myDeviceToken = memberDto.getMyDeviceToken();
		memberDao.coupleDeviceToken = memberDto.getCoupleDeviceToken();

		memberDao.myProfileImgAddr = memberDto.getMyProfileImgAddr();
		memberDao.coupleProfileImgAddr = memberDto.getCoupleProfileImgAddr();
		memberDao.mainBannerImgAddr = memberDto.getMainBannerImgAddr();

		memberDao.expression = memberDto.getExpression();

		if (memberDto.getStatus() == null) {
			memberDao.status = MemberStatus.ACTIVE;
		} else {
			memberDao.status = memberDto.getStatus();
		}

		if (memberDto.getUserRole() == null) {
			memberDao.userRole = "USER";
		} else {
			memberDao.userRole = memberDto.getUserRole();
		}
		memberDao.regDt = LocalDateTime.now();
		memberDao.modDt = memberDto.getModDt();
		memberDao.coupleRegDt = memberDto.getCoupleRegDt();

		return memberDao;
	}

	private static void validate(MemberDto memberDto, String encodedPassword) {
		if (memberDto.getNickName() == null) {
			throw new NullPointerException("nickName 필드에 값이 없습니다.");
		}

		if (memberDto.getEmail() == null) {
			throw new NullPointerException("Email 필드에 값이 없습니다.");
		}

		if (memberDto.getCoupleCode() == null || memberDto.getCoupleCode().equals("")) {
			throw new NullPointerException("coupleCode 필드에 값이 없습니다.");
		}
	}
}
