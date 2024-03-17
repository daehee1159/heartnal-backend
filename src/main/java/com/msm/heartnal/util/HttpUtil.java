package com.msm.heartnal.util;

import org.springframework.web.bind.annotation.CrossOrigin;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 최대희
 * @since 2021-06-17
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8180" })
public class HttpUtil {
	public static String getRequestURI( HttpServletRequest request) {

		return request.getRequestURI();
	}
}
