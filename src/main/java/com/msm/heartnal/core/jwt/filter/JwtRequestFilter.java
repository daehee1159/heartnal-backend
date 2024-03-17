package com.msm.heartnal.core.jwt.filter;

import com.msm.heartnal.core.jwt.JwtComponent;
import com.msm.heartnal.core.jwt.enums.CommonException;
import com.msm.heartnal.core.jwt.service.JwtService;
import com.msm.heartnal.util.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 최대희
 * @since 2021-06-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	private final JwtComponent jwtComponent;

	@Value("/get_access_token")
	private String accessTokenUrl;

	/**
	 *
	 * @param request
	 * @param response
	 * @param filterChain
	 * @throws CommonException
	 */
	private void run(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws CommonException {
		// 헤더에서 인증값 꺼냄
		final String authHeader = request.getHeader("Authorization");

		String username = null;
		String jwt = null;

		// 기본값은 access token으로 세팅
		JwtComponent.TOKEN_TYPE tokenType = JwtComponent.TOKEN_TYPE.ACCESS_TOKEN;

		// 인증값이 있고, Bearer 헤더가 았을때처리
		if( authHeader != null && authHeader.startsWith("Bearer ")) {

			// 토큰값만 추출함
			jwt = authHeader.substring(7);

			// refresh token으로 access token 재발급인지 확인
			String requestURI = HttpUtil.getRequestURI(request);
			if( this.accessTokenUrl != null && this.accessTokenUrl.equalsIgnoreCase( requestURI)) {
				tokenType = JwtComponent.TOKEN_TYPE.REFRESH_TOKEN;
			}

			// 토큰값와 타입으로 사용자아이디를 조회함
			username = this.jwtComponent.extractUsername(jwt, tokenType);
		}

		// 사용자아이디가 있고, Security에 인증값이 없을때 처리
		if( username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			// 사용자 아이디로 해당 정보가 있는지 확인함
			UserDetails userDetails = this.jwtService.loadUserByUsername(username);

			// 토큰의 유효성을 확인
			if( this.jwtComponent.validateToken( jwt, userDetails, tokenType)) {

				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken( userDetails, null, userDetails.getAuthorities());
				token.setDetails( new WebAuthenticationDetailsSource().buildDetails( request));

				// Security에 인증값 추가
				SecurityContextHolder.getContext().setAuthentication( token);

			}
		}

	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		try {
			this.run(request, response, filterChain);
		} catch (CommonException e) {
			log.error("Client IP 주소 : " + request.getRemoteAddr());
			e.printStackTrace();
		}

		filterChain.doFilter(request, response);

	}
}
