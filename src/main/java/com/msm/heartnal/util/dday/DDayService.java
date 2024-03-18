package com.msm.heartnal.util.dday;

import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.AnniversaryDto;
import com.msm.heartnal.core.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author 최대희
 * @since 2021-10-26
 * 100일 단위 기념일 제공 + 1년 단위 기념일 제공
 * 각 D-Day 남은 기간에 따른 향후 1년 데이터 + 마지막에 걸친 날짜의 경우 남은 D-Day 보여주는게 너무 없어서 다음 1개 D-Day 를 더 보여줌
 */
@Service
@RequiredArgsConstructor
public class DDayService {
	private final MemberMapper memberMapper;

	public List<AnniversaryDto> getAnniversary(String username) throws Exception {
		MemberDao memberDao = memberMapper.getMemberInfoByUsername(username);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date coupleRegDt = dateFormat.parse(memberDao.getCoupleRegDt());

		Date today = new Date();

		long gap = today.getTime() - coupleRegDt.getTime();

		// 네이버 D-Day 계산기는 기준일을 1일로 잡는 반면 Calendar 클래스는 기준일을 1일로 잡지 않음
		// 여기서 +1일은 그 차이때문에 해주는 것
		long days = gap / (24 * 60 * 60 * 1000) + 1;

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		List<AnniversaryDto> anniversaryList = memberMapper.getAnniversaryByUsername(username);
		List<AnniversaryDto> result = new ArrayList<>();

		// 1. mapper 로 불러온 데이터를 반복여부에 따라 result 에 넣기
		for (AnniversaryDto anniversary : anniversaryList) {
			Calendar calendar = Calendar.getInstance();
			Date anniversaryDate = dateFormat.parse(anniversary.getAnniversaryDate());
			if (anniversary.getRepeatYN().equals("Y")) {
				// 불러온 기념일이 이번년도 날짜 지났는지 체크
				boolean passByDate = passByDate(anniversaryDate);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime parseDate = LocalDateTime.parse(df.format(anniversaryDate.getTime()) + " 00:00:00", formatter);
				LocalDateTime now = LocalDateTime.now();

				TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");

				Calendar theDate = Calendar.getInstance(tz);

				if (passByDate) {
					// calendar 에 년도를 이번년도로 먼저 세팅해야함
					// 이번년도 날짜가 지난 경우는 result 에 내년 년도를 세팅해서 넣으면 됨
					theDate.set(now.getYear() + 1, parseDate.getMonthValue() - 1, parseDate.getDayOfMonth());
				} else {
					// 이번년도 날짜가 지나지 않은 경우는 result 에 년도를 이번년도로 세팅해서 넣으면 됨
					theDate.set(now.getYear(), parseDate.getMonthValue() - 1, parseDate.getDayOfMonth());
				}
				calendar.setTime(theDate.getTime());

				AnniversaryDto anniversaryDto = new AnniversaryDto();
				anniversaryDto.setAnniversarySeq(anniversary.getAnniversarySeq());
				anniversaryDto.setAnniversaryDate(df.format(calendar.getTime()));
				anniversaryDto.setAnniversaryTitle(anniversary.getAnniversaryTitle());
				anniversaryDto.setRemainingDays(calcDate(calendar.getTime()));
				anniversaryDto.setRepeatYN("Y");

				result.add(anniversaryDto);
			} else {
				// 불러온 기념일이 이번년도 날짜 지났는지 체크
				boolean passByDate = passByDate(anniversaryDate);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime parseDate = LocalDateTime.parse(df.format(anniversaryDate.getTime()) + " 00:00:00", formatter);
				LocalDateTime now = LocalDateTime.now();

				TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");

				Calendar theDate = Calendar.getInstance(tz);

				AnniversaryDto anniversaryDto = new AnniversaryDto();

				if (!passByDate) {
					// passByDate == true 면 날짜 지난거임
					// 이번년도 날짜가 지난 경우는 result 에 넣을필요 없음
					// calendar 에 년도를 이번년도로 먼저 세팅해야함
					// 이번년도 날짜가 지나지 않은 경우는 result 에 내년 년도를 세팅해서 넣으면 됨
					theDate.set(parseDate.getYear(), parseDate.getMonthValue() - 1, parseDate.getDayOfMonth());

					calendar.setTime(theDate.getTime());

					anniversaryDto.setAnniversarySeq(anniversary.getAnniversarySeq());
					anniversaryDto.setAnniversaryDate(df.format(calendar.getTime()));
					anniversaryDto.setAnniversaryTitle(anniversary.getAnniversaryTitle());
					anniversaryDto.setRemainingDays(calcDate(calendar.getTime()));

					anniversaryDto.setRepeatYN("N");

					// D-Day인 경우 담아줘야함 그리고 D-Day가 아닌경우에서 0보다 작으면 담아줌
					if (anniversaryDto.getRemainingDays().equals("D-Day")) {
						result.add(anniversaryDto);
					} else if (Integer.parseInt(anniversaryDto.getRemainingDays().replaceAll("D", "")) <= 0) {
						result.add(anniversaryDto);
					}
				}
			}
		}

		int defaultDays = 365;
		int anniversaryYear = 1;
		int anniversaryDay = 100;
		int cnt = 1;

		while (true) {
			if (days < defaultDays) {
				// 100일,200일,300일,400일 데이터 필요
				// for문 무조건 4번 돌기
				for (int i = 0; i < 4; i++) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(coupleRegDt);

					calendar.add(Calendar.DATE, anniversaryDay);
					AnniversaryDto anniversaryDto = new AnniversaryDto();
					anniversaryDto.setAnniversaryDate(df.format(calendar.getTime()));
					anniversaryDto.setAnniversaryTitle(anniversaryDay + "일");
					anniversaryDto.setRemainingDays(calcDate(calendar.getTime()));

					anniversaryDay += 100;
					result.add(anniversaryDto);
				}
				// 그 다음 몇주년인지도 이벤트 추가
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(coupleRegDt);
				calendar.add(Calendar.DATE, defaultDays);
				AnniversaryDto anniversaryDto = new AnniversaryDto();
				anniversaryDto.setAnniversaryDate(df.format(calendar.getTime()));
				anniversaryDto.setAnniversaryTitle(anniversaryYear + "주년");
				anniversaryDto.setRemainingDays(calcDate(calendar.getTime()));
				result.add(anniversaryDto);

				break;
			} else {
				defaultDays += 365;
				anniversaryDay += 400;
				cnt++;
				anniversaryYear++;
			}
		}

		AnniversaryDateComparator comparator = new AnniversaryDateComparator();
		// 오름차순
		result.sort(comparator);

		return result;
	}

	/**
	 * 해당 D-Day 가 이번년도 지났는지 여부
	 */
	private static boolean passByDate(Date date) throws Exception {
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime parseDate = LocalDateTime.parse(df.format(date.getTime()) + " 00:00:00",formatter);
			LocalDateTime now = LocalDateTime.now();

			TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");

			Calendar today = Calendar.getInstance(tz);
			Calendar dDay = Calendar.getInstance(tz);

			dDay.set(now.getYear(), parseDate.getMonthValue()-1, parseDate.getDayOfMonth());

			long cnt_dDay = dDay.getTimeInMillis() / 86400000;
			long cnt_today = today.getTimeInMillis() / 86400000;
			long sub = cnt_today - cnt_dDay;

			return (int) sub > 0;

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	/**
	 * 날짜 오름차순, 내림차순
	 */
	static class AnniversaryDateComparator implements Comparator<AnniversaryDto> {
		@Override
		public int compare(AnniversaryDto r1, AnniversaryDto r2) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

			Date day1 = null;
			Date day2 = null;

			try {
				day1 = format.parse(r1.getAnniversaryDate());
				day2 = format.parse(r2.getAnniversaryDate());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			int result = day1.compareTo(day2);
			return result;
		}
	}

	/**
	 * D-Day 남은 기간 계산
	 */
	private static String calcDate(Date date) {
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime parseRemainingDays = LocalDateTime.parse(df.format(date.getTime()) + " 00:00:00",formatter);

			TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");
			Calendar today = Calendar.getInstance(tz);
			Calendar dDay = Calendar.getInstance(tz);

			// Java Calendar 클래스는 Calendar.MONTH 가 0이면 1월이기 때문에 -1 해줘야 함
			dDay.set(parseRemainingDays.getYear(), parseRemainingDays.getMonthValue()-1, parseRemainingDays.getDayOfMonth());
			long cnt_dDay = dDay.getTimeInMillis() / 86400000;
			long cnt_today = today.getTimeInMillis() / 86400000;
			long sub = cnt_today - cnt_dDay;

			if ((int) sub == 0) {
				return "D-Day";
			} if ((int) sub > 0) {
				return "D+" + (int) sub;
			} else {
				return "D" + (int) sub;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

}
