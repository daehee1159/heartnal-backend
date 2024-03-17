package com.msm.heartnal.api.config;

import com.msm.heartnal.core.jwt.JwtComponent;
import com.msm.heartnal.core.jwt.filter.JwtRequestFilter;
import com.msm.heartnal.core.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.PostConstruct;

/**
 * @author 최대희
 * @since 2021-06-17
 */
@Configuration
@EnableWebSecurity
@PropertySource("classpath:application.yml")
@RequiredArgsConstructor
public class JwtSecurityConfig extends WebSecurityConfigurerAdapter {

	private JwtRequestFilter jwtRequestFilter;

	private final JwtComponent jwtComponent;

	private final JwtService jwtService;

	// 순환참조 피하려고 일단 조치
	@PostConstruct
	public void initialize() {
		jwtRequestFilter = new JwtRequestFilter(jwtService, jwtComponent);
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		auth.userDetailsService( jwtService);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.httpBasic().disable()
			.authorizeRequests()
			.antMatchers("/authenticate", "/api/**", "/upload/**", "/get_access_token", "/fcm/**", "/eat-signal/**", "/play-signal/**").permitAll()
			.anyRequest().authenticated()
			.and()
			.sessionManagement()
			.sessionCreationPolicy( SessionCreationPolicy.STATELESS);

		http
			.addFilterBefore( jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

	}
}
