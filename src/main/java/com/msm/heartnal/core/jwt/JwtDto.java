package com.msm.heartnal.core.jwt;

import lombok.Getter;

/**
 * @author 최대희
 * @since 2021-06-17
 */
@Getter
public class JwtDto {

	private final String accessToken;
	private final String refreshToken;

	private final String username;
	private final String password;


	public JwtDto(String accessToken, String refreshToken, String username, String password) {
		super();
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.username = username;
		this.password = password;
	}

	@Override
	public String toString() {
		return "Jwt [accessToken=" + accessToken + ", refreshToken=" + refreshToken + "]";
	}

}
