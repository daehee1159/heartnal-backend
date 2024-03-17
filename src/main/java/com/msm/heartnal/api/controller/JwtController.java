package com.msm.heartnal.api.controller;

import com.msm.heartnal.api.service.MemberService;
import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.AuthenticationMemberDto;
import com.msm.heartnal.core.dto.AuthenticationRequest;
import com.msm.heartnal.core.dto.AuthenticationResponse;
import com.msm.heartnal.core.jwt.JwtComponent;
import com.msm.heartnal.core.jwt.JwtDto;
import com.msm.heartnal.core.jwt.enums.CommonException;
import com.msm.heartnal.core.jwt.service.JwtService;
import com.msm.heartnal.util.HttpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 최대희
 * @since 2021-07-02
 */
@RestController
@RequiredArgsConstructor
public class JwtController {
	private final JwtComponent jwtUtil;

	private final JwtService jwtService;

	private final MemberService memberService;

	@Value("/get_access_token")
	private String accessTokenUrl;

	private final JwtComponent jwtComponent;

	/**
	 * 토큰생성요청
	 * @param authenticationRequest
	 * @return
	 * @throws CommonException, Exception
	 * @throws Exception
	 */
	@RequestMapping( value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> authenticate (@RequestBody AuthenticationRequest authenticationRequest) throws CommonException, Exception{

		JwtDto token = this.jwtUtil.makeJwt(authenticationRequest.getUsername(), authenticationRequest.getPassword());

		return ResponseEntity.ok(  new AuthenticationResponse(token));
	}

	/**
	 * 리프레시토큰으로 토큰재발급
	 * @return
	 * @throws Exception
	 */
	@RequestMapping( value = "/get_access_token", method = RequestMethod.POST)
	public ResponseEntity<?> get_access_token(HttpServletRequest httpServletRequest) throws Exception{
		JwtDto token = this.jwtUtil.makeReJwt();
		return ResponseEntity.ok(  new AuthenticationResponse(token));
	}

	@RequestMapping( value = "/authenticate", method = RequestMethod.GET)
	public ResponseEntity<?> me (HttpServletRequest httpServletRequest) throws CommonException, Exception{

		// 헤더에서 인증값 꺼냄
		final String authHeader = httpServletRequest.getHeader("Authorization");

		String username = null;
		String jwt = null;

		// 기본값은 access token으로 세팅
		JwtComponent.TOKEN_TYPE tokenType = JwtComponent.TOKEN_TYPE.ACCESS_TOKEN;

		// 인증값이 있고, Bearer 헤더가 았을때처리
		if( authHeader != null && authHeader.startsWith("Bearer ")) {

			// 토큰값만 추출함
			jwt = authHeader.substring(7);

			// refresh token으로 access token 재발급인지 확인
			String requestURI = HttpUtil.getRequestURI(httpServletRequest);
			if( this.accessTokenUrl.equalsIgnoreCase( requestURI)) {
				tokenType = JwtComponent.TOKEN_TYPE.REFRESH_TOKEN;
			}

			// 토큰값와 타입으로 사용자아이디를 조회함
			username = this.jwtComponent.extractUsername(jwt, tokenType);
		}

		UserDetails userDetails = this.jwtService.loadUserByUsername(username);

		MemberDao memberDao = memberService.getMemberInfoByUsername(username);


		AuthenticationMemberDto member = new AuthenticationMemberDto();
		member.setMemberSeq(memberDao.getMemberSeq());
		member.setUsername(userDetails.getUsername());
		member.setEmail(memberDao.getEmail());


		return ResponseEntity.ok(member);
	}
}
