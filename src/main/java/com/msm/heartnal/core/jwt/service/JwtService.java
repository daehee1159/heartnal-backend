package com.msm.heartnal.core.jwt.service;

import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author 최대희
 * @since 2021-06-17
 */
@Service
@RequiredArgsConstructor
public class JwtService implements UserDetailsService {

	private final MemberMapper memberMapper;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// id, pw 검증 로직
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(username);

		return User.builder()
			.username(memberDao.getUsername())
			.password(memberDao.getPassword())
			.roles(memberDao.getUserRole())
			.build();
	}
}
