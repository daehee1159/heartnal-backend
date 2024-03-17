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
 * D-Day 기능이 MemberService 에서 코드가 너무 길어서 가독성을 떨어뜨림 그래서 이쪽으로 따로 나눔
 * 1 ~ 10년차는 100일 단위 제공 + 1년 단위 제공
 * 11 ~ 20년차 1년 단위 제공 + 1000일 단위 제공
 * 21년차 이상은 30년차까지 1년 단위 제공 + 1000일 단위 제공, 30년, 40년, 50년 3개 제공
 * 각 D-Day 남은 기간에 따른 향후 1년 데이터 + 마지막에 걸친 날짜의 경우 남은 D-Day 보여주는게 너무 없어서 다음 1개 D-Day 를 더 보여줌
 * *********************************  리팩토링 무조건 해야함!!!! *********************************
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
//		System.out.println(gap / (24 * 60 * 60 * 1000) + 1 + "일 (" + coupleRegDt + " ~ " + today + ")");
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

				} else {
//					result.add(anniversaryDto);
				}
			}
		}

		// 1년 미만
		// TODO 여기 코드 효율적으로 짜야함... 리팩토링 반드시 필요!!!
		if (days < 365) {
//			System.out.println("days < 365");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);

			// 100일
			calendar.add(Calendar.DATE, 99);
			AnniversaryDto hundred = new AnniversaryDto();
			hundred.setAnniversaryDate(df.format(calendar.getTime()));
			hundred.setAnniversaryTitle("100일");
			hundred.setRemainingDays(calcDate(calendar.getTime()));

			// 200일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoHundred = new AnniversaryDto();
			twoHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoHundred.setAnniversaryTitle("200일");
			twoHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 300일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto threeHundred = new AnniversaryDto();
			threeHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeHundred.setAnniversaryTitle("300일");
			threeHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 365일 처음 값으로 되돌리고 1년 추가
			calendar.add(Calendar.DATE, -299);
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto firstYear = new AnniversaryDto();
			firstYear.setAnniversaryDate(df.format(calendar.getTime()));
			firstYear.setAnniversaryTitle("1주년");
			firstYear.setRemainingDays(calcDate(calendar.getTime()));

			// 400일 (days 후반부에 더 보여줄 데이터)
			calendar.add(Calendar.DATE, 399);
			calendar.add(Calendar.YEAR, -1);
			AnniversaryDto fourHundred = new AnniversaryDto();
			fourHundred.setAnniversaryDate(df.format(calendar.getTime()));
			fourHundred.setAnniversaryTitle("400일");
			fourHundred.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 100) {
				result.add(hundred);
				result.add(twoHundred);
				result.add(threeHundred);
				result.add(firstYear);
			} else if (days < 200) {
				result.add(twoHundred);
				result.add(threeHundred);
				result.add(firstYear);
			} else if (days < 300) {
				result.add(threeHundred);
				result.add(firstYear);
				result.add(fourHundred);
			} else {
				result.add(firstYear);
				result.add(fourHundred);
			}
		}
		else if (days < 730) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);

			// 400일
			calendar.add(Calendar.DATE, 399);
			AnniversaryDto fourHundred = new AnniversaryDto();
			fourHundred.setAnniversaryDate(df.format(calendar.getTime()));
			fourHundred.setAnniversaryTitle("400일");
			fourHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 500일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto fiveHundred = new AnniversaryDto();
			fiveHundred.setAnniversaryDate(df.format(calendar.getTime()));
			fiveHundred.setAnniversaryTitle("500일");
			fiveHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 600일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto sixHundred = new AnniversaryDto();
			sixHundred.setAnniversaryDate(df.format(calendar.getTime()));
			sixHundred.setAnniversaryTitle("600일");
			sixHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 700일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto sevenHundred = new AnniversaryDto();
			sevenHundred.setAnniversaryDate(df.format(calendar.getTime()));
			sevenHundred.setAnniversaryTitle("700일");
			sevenHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 2주년 730
			calendar.add(Calendar.DATE, -699);
			calendar.add(Calendar.YEAR, 2);
			AnniversaryDto secondYear = new AnniversaryDto();
			secondYear.setAnniversaryDate(df.format(calendar.getTime()));
			secondYear.setAnniversaryTitle("2주년");
			secondYear.setRemainingDays(calcDate(calendar.getTime()));

			// 800일 (days 후반부에 더 보여줄 데이터)
			calendar.add(Calendar.DATE, 799);
			calendar.add(Calendar.YEAR, -2);
			AnniversaryDto eightHundred = new AnniversaryDto();
			eightHundred.setAnniversaryDate(df.format(calendar.getTime()));
			eightHundred.setAnniversaryTitle("800일");
			eightHundred.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 400) {
				result.add(fourHundred);
				result.add(fiveHundred);
				result.add(sixHundred);
				result.add(sevenHundred);
				result.add(secondYear);
			} else if (days < 500) {
				result.add(fiveHundred);
				result.add(sixHundred);
				result.add(sevenHundred);
				result.add(secondYear);
			} else if (days < 600) {
				result.add(sixHundred);
				result.add(sevenHundred);
				result.add(secondYear);
			} else if (days < 700) {
				result.add(sevenHundred);
				result.add(secondYear);
				result.add(eightHundred);
			} else {
				result.add(secondYear);
				result.add(eightHundred);
			}
//			System.out.println("days > 365 && days < 730");
		}
		else if (days < 1095) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);

			// 800일
			calendar.add(Calendar.DATE, 799);
			AnniversaryDto eightHundred = new AnniversaryDto();
			eightHundred.setAnniversaryDate(df.format(calendar.getTime()));
			eightHundred.setAnniversaryTitle("800일");
			eightHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 900일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto nineHundred = new AnniversaryDto();
			nineHundred.setAnniversaryDate(df.format(calendar.getTime()));
			nineHundred.setAnniversaryTitle("900일");
			nineHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 1000일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto thousand = new AnniversaryDto();
			thousand.setAnniversaryDate(df.format(calendar.getTime()));
			thousand.setAnniversaryTitle("1000일");
			thousand.setRemainingDays(calcDate(calendar.getTime()));

			// 3주년
			calendar.add(Calendar.DATE, -999);
			calendar.add(Calendar.YEAR, 3);
			AnniversaryDto thirdYear = new AnniversaryDto();
			thirdYear.setAnniversaryDate(df.format(calendar.getTime()));
			thirdYear.setAnniversaryTitle("3주년");
			thirdYear.setRemainingDays(calcDate(calendar.getTime()));

			// 1100일
			calendar.add(Calendar.DATE, 1099);
			calendar.add(Calendar.YEAR, -3);
			AnniversaryDto oneThousandOneHundred = new AnniversaryDto();
			oneThousandOneHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandOneHundred.setAnniversaryTitle("1100일");
			oneThousandOneHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 1200일 (days 후반부에 더 보여줄 데이터)
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto oneThousandTwoHundred = new AnniversaryDto();
			oneThousandTwoHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandTwoHundred.setAnniversaryTitle("1200일");
			oneThousandTwoHundred.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 800) {
				result.add(eightHundred);
				result.add(nineHundred);
				result.add(thousand);
				result.add(thirdYear);
				result.add(oneThousandOneHundred);
			} else if (days < 900) {
				result.add(nineHundred);
				result.add(thousand);
				result.add(thirdYear);
				result.add(oneThousandOneHundred);
			} else if (days < 1000) {
				result.add(thousand);
				result.add(thirdYear);
				result.add(oneThousandOneHundred);
				result.add(oneThousandTwoHundred);
			} else {
				result.add(thirdYear);
				result.add(oneThousandOneHundred);
				result.add(oneThousandTwoHundred);
			}
//			System.out.println("days > 730 && days < 1095");
		}
		else if (days < 1460) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);

			// 1100일
			calendar.add(Calendar.DATE, 1099);
			AnniversaryDto oneThousandOneHundred = new AnniversaryDto();
			oneThousandOneHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandOneHundred.setAnniversaryTitle("1100일");
			oneThousandOneHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 1200일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto oneThousandTwoHundred = new AnniversaryDto();
			oneThousandTwoHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandTwoHundred.setAnniversaryTitle("1200일");
			oneThousandTwoHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 1300일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto oneThousandThreeHundred = new AnniversaryDto();
			oneThousandThreeHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandThreeHundred.setAnniversaryTitle("1300일");
			oneThousandThreeHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 1400일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto oneThousandFourHundred = new AnniversaryDto();
			oneThousandFourHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandFourHundred.setAnniversaryTitle("1400일");
			oneThousandFourHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 4주년
			calendar.add(Calendar.DATE, -1399);
			calendar.add(Calendar.YEAR, 4);
			AnniversaryDto fourthYear = new AnniversaryDto();
			fourthYear.setAnniversaryDate(df.format(calendar.getTime()));
			fourthYear.setAnniversaryTitle("4주년");
			fourthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 1500일
			calendar.add(Calendar.DATE, 1499);
			calendar.add(Calendar.YEAR, -4);
			AnniversaryDto oneThousandFiveHundred = new AnniversaryDto();
			oneThousandFiveHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandFiveHundred.setAnniversaryTitle("1500일");
			oneThousandFiveHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 1600일 (days 후반부에 더 보여줄 데이터)
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto oneThousandSixHundred = new AnniversaryDto();
			oneThousandSixHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandSixHundred.setAnniversaryTitle("1600일");
			oneThousandSixHundred.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 1100) {
				result.add(oneThousandOneHundred);
				result.add(oneThousandTwoHundred);
				result.add(oneThousandThreeHundred);
				result.add(oneThousandFourHundred);
				result.add(fourthYear);
				result.add(oneThousandFiveHundred);
			} else if (days < 1200) {
				result.add(oneThousandTwoHundred);
				result.add(oneThousandThreeHundred);
				result.add(oneThousandFourHundred);
				result.add(fourthYear);
				result.add(oneThousandFiveHundred);
			} else if (days < 1300) {
				result.add(oneThousandThreeHundred);
				result.add(oneThousandFourHundred);
				result.add(fourthYear);
				result.add(oneThousandFiveHundred);
			} else if (days < 1400) {
				result.add(oneThousandFourHundred);
				result.add(fourthYear);
				result.add(oneThousandFiveHundred);
				result.add(oneThousandSixHundred);
			} else {
				result.add(fourthYear);
				result.add(oneThousandFiveHundred);
				result.add(oneThousandSixHundred);
			}
//			System.out.println("days > 1095 && days < 1460");
		}
		else if (days < 1825) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);
			// 1500일
			calendar.add(Calendar.DATE, 1499);

			AnniversaryDto oneThousandFiveHundred = new AnniversaryDto();
			oneThousandFiveHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandFiveHundred.setAnniversaryTitle("1500일");
			oneThousandFiveHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 1600일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto oneThousandSixHundred = new AnniversaryDto();
			oneThousandSixHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandSixHundred.setAnniversaryTitle("1600일");
			oneThousandSixHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 1700일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto oneThousandSevenHundred = new AnniversaryDto();
			oneThousandSevenHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandSevenHundred.setAnniversaryTitle("1700일");
			oneThousandSevenHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 1800일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto oneThousandEightHundred = new AnniversaryDto();
			oneThousandEightHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandEightHundred.setAnniversaryTitle("1800일");
			oneThousandEightHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 5주년
			calendar.add(Calendar.DATE, -1799);
			calendar.add(Calendar.YEAR, 5);
			AnniversaryDto fifthYear = new AnniversaryDto();
			fifthYear.setAnniversaryDate(df.format(calendar.getTime()));
			fifthYear.setAnniversaryTitle("5주년");
			fifthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 1900일
			calendar.add(Calendar.DATE, 1899);
			calendar.add(Calendar.YEAR, -5);
			AnniversaryDto oneThousandNineHundred = new AnniversaryDto();
			oneThousandNineHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandNineHundred.setAnniversaryTitle("1900일");
			oneThousandNineHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 2000일 (days 후반부에 더 보여줄 데이터)
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoThousand = new AnniversaryDto();
			twoThousand.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousand.setAnniversaryTitle("2000일");
			twoThousand.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 1500) {
				result.add(oneThousandFiveHundred);
				result.add(oneThousandSixHundred);
				result.add(oneThousandSevenHundred);
				result.add(oneThousandEightHundred);
				result.add(fifthYear);
				result.add(oneThousandNineHundred);
			} else if (days < 1600) {
				result.add(oneThousandSixHundred);
				result.add(oneThousandSevenHundred);
				result.add(oneThousandEightHundred);
				result.add(fifthYear);
				result.add(oneThousandNineHundred);
			} else if (days < 1700) {
				result.add(oneThousandSevenHundred);
				result.add(oneThousandEightHundred);
				result.add(fifthYear);
				result.add(oneThousandNineHundred);
			} else if (days < 1800) {
				result.add(oneThousandEightHundred);
				result.add(fifthYear);
				result.add(oneThousandNineHundred);
				result.add(twoThousand);
			} else {
				result.add(fifthYear);
				result.add(oneThousandFiveHundred);
				result.add(twoThousand);
			}
//			System.out.println("days < 1825");
		}
		else if (days < 2190) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);

			// 1900일
			calendar.add(Calendar.DATE, 1899);
			AnniversaryDto oneThousandNineHundred = new AnniversaryDto();
			oneThousandNineHundred.setAnniversaryDate(df.format(calendar.getTime()));
			oneThousandNineHundred.setAnniversaryTitle("1900일");
			oneThousandNineHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 2000일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoThousand = new AnniversaryDto();
			twoThousand.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousand.setAnniversaryTitle("2000일");
			twoThousand.setRemainingDays(calcDate(calendar.getTime()));

			// 2100일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoThousandOneHundred = new AnniversaryDto();
			twoThousandOneHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandOneHundred.setAnniversaryTitle("2100일");
			twoThousandOneHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 6주년
			calendar.add(Calendar.DATE, -2099);
			calendar.add(Calendar.YEAR, 6);
			AnniversaryDto sixthYear = new AnniversaryDto();
			sixthYear.setAnniversaryDate(df.format(calendar.getTime()));
			sixthYear.setAnniversaryTitle("6주년");
			sixthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 2200일
			calendar.add(Calendar.DATE, 2199);
			calendar.add(Calendar.YEAR, -6);
			AnniversaryDto twoThousandTwoHundred = new AnniversaryDto();
			twoThousandTwoHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandTwoHundred.setAnniversaryTitle("2200일");
			twoThousandTwoHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 2300일 (days 후반부에 더 보여줄 데이터)
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoThousandThreeHundred = new AnniversaryDto();
			twoThousandThreeHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandThreeHundred.setAnniversaryTitle("2300일");
			twoThousandThreeHundred.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 1900) {
				result.add(oneThousandNineHundred);
				result.add(twoThousand);
				result.add(twoThousandOneHundred);
				result.add(sixthYear);
				result.add(twoThousandTwoHundred);
			} else if (days < 2000) {
				result.add(twoThousand);
				result.add(twoThousandOneHundred);
				result.add(sixthYear);
				result.add(twoThousandTwoHundred);
			} else if (days < 2100) {
				result.add(twoThousandOneHundred);
				result.add(sixthYear);
				result.add(twoThousandTwoHundred);
				result.add(twoThousandThreeHundred);
			} else {
				result.add(sixthYear);
				result.add(twoThousandTwoHundred);
				result.add(twoThousandThreeHundred);
			}
//			System.out.println("days < 2190");
		}
		else if (days < 2555) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);

			// 2200일
			calendar.add(Calendar.DATE, 2199);
			AnniversaryDto twoThousandTwoHundred = new AnniversaryDto();
			twoThousandTwoHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandTwoHundred.setAnniversaryTitle("2200일");
			twoThousandTwoHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 2300일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoThousandThreeHundred = new AnniversaryDto();
			twoThousandThreeHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandThreeHundred.setAnniversaryTitle("2300일");
			twoThousandThreeHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 2400일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoThousandFourHundred = new AnniversaryDto();
			twoThousandFourHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandFourHundred.setAnniversaryTitle("2400일");
			twoThousandFourHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 2500일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoThousandFiveHundred = new AnniversaryDto();
			twoThousandFiveHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandFiveHundred.setAnniversaryTitle("2500일");
			twoThousandFiveHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 7주년
			calendar.add(Calendar.DATE, -2499);
			calendar.add(Calendar.YEAR, 7);
			AnniversaryDto seventhYear = new AnniversaryDto();
			seventhYear.setAnniversaryDate(df.format(calendar.getTime()));
			seventhYear.setAnniversaryTitle("7주년");
			seventhYear.setRemainingDays(calcDate(calendar.getTime()));

			// 2600일
			calendar.add(Calendar.DATE, 2599);
			calendar.add(Calendar.YEAR, -7);
			AnniversaryDto twoThousandSixHundred = new AnniversaryDto();
			twoThousandSixHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandSixHundred.setAnniversaryTitle("2600일");
			twoThousandSixHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 2700일 (days 후반부에 더 보여줄 데이터)
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoThousandSevenHundred = new AnniversaryDto();
			twoThousandSevenHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandSevenHundred.setAnniversaryTitle("2700일");
			twoThousandSevenHundred.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 2200) {
				result.add(twoThousandTwoHundred);
				result.add(twoThousandThreeHundred);
				result.add(twoThousandFourHundred);
				result.add(twoThousandFiveHundred);
				result.add(seventhYear);
				result.add(twoThousandSixHundred);
			} else if (days < 2300) {
				result.add(twoThousandThreeHundred);
				result.add(twoThousandFourHundred);
				result.add(twoThousandFiveHundred);
				result.add(seventhYear);
				result.add(twoThousandSixHundred);
			} else if (days < 2400) {
				result.add(twoThousandFourHundred);
				result.add(twoThousandFiveHundred);
				result.add(seventhYear);
				result.add(twoThousandSixHundred);
			} else if (days < 2500) {
				result.add(twoThousandFiveHundred);
				result.add(seventhYear);
				result.add(twoThousandSixHundred);
				result.add(twoThousandSevenHundred);
			} else {
				result.add(seventhYear);
				result.add(twoThousandTwoHundred);
				result.add(twoThousandSevenHundred);
			}
//			System.out.println("days < 2555");
		}
		else if (days < 2920) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);

			// 2600일
			calendar.add(Calendar.DATE, 2599);
			AnniversaryDto twoThousandSixHundred = new AnniversaryDto();
			twoThousandSixHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandSixHundred.setAnniversaryTitle("2600일");
			twoThousandSixHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 2700일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoThousandSevenHundred = new AnniversaryDto();
			twoThousandSevenHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandSevenHundred.setAnniversaryTitle("2700일");
			twoThousandSevenHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 2800일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoThousandEightHundred = new AnniversaryDto();
			twoThousandEightHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandEightHundred.setAnniversaryTitle("2800일");
			twoThousandEightHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 2900일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto twoThousandNineHundred = new AnniversaryDto();
			twoThousandNineHundred.setAnniversaryDate(df.format(calendar.getTime()));
			twoThousandNineHundred.setAnniversaryTitle("2900일");
			twoThousandNineHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 8주년
			calendar.add(Calendar.DATE, -2899);
			calendar.add(Calendar.YEAR, 8);
			AnniversaryDto eighthYear = new AnniversaryDto();
			eighthYear.setAnniversaryDate(df.format(calendar.getTime()));
			eighthYear.setAnniversaryTitle("8주년");
			eighthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 3000일
			calendar.add(Calendar.DATE, 2999);
			calendar.add(Calendar.YEAR, -8);
			AnniversaryDto threeThousand = new AnniversaryDto();
			threeThousand.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousand.setAnniversaryTitle("3000일");
			threeThousand.setRemainingDays(calcDate(calendar.getTime()));

			// 3100일 (days 후반부에 더 보여줄 데이터)
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto threeThousandOneHundred = new AnniversaryDto();
			threeThousandOneHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousandOneHundred.setAnniversaryTitle("3100일");
			threeThousandOneHundred.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 2600) {
				result.add(twoThousandSixHundred);
				result.add(twoThousandSevenHundred);
				result.add(twoThousandEightHundred);
				result.add(twoThousandNineHundred);
				result.add(eighthYear);
				result.add(threeThousand);
			} else if (days < 2700) {
				result.add(twoThousandSevenHundred);
				result.add(twoThousandEightHundred);
				result.add(twoThousandNineHundred);
				result.add(eighthYear);
				result.add(threeThousand);
			} else if (days < 2800) {
				result.add(twoThousandEightHundred);
				result.add(twoThousandNineHundred);
				result.add(eighthYear);
				result.add(threeThousand);
			} else if (days < 2900) {
				result.add(twoThousandNineHundred);
				result.add(eighthYear);
				result.add(threeThousand);
				result.add(threeThousandOneHundred);
			} else {
				result.add(eighthYear);
				result.add(threeThousand);
				result.add(threeThousandOneHundred);
			}
//			System.out.println("days < 2920");
		}
		else if (days < 3285) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);

			// 3000일
			calendar.add(Calendar.DATE, 2999);
			AnniversaryDto threeThousand = new AnniversaryDto();
			threeThousand.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousand.setAnniversaryTitle("3000일");
			threeThousand.setRemainingDays(calcDate(calendar.getTime()));

			// 3100일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto threeThousandOneHundred = new AnniversaryDto();
			threeThousandOneHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousandOneHundred.setAnniversaryTitle("3100일");
			threeThousandOneHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 3200일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto threeThousandTwoHundred = new AnniversaryDto();
			threeThousandTwoHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousandTwoHundred.setAnniversaryTitle("3200일");
			threeThousandTwoHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 9주년
			calendar.add(Calendar.DATE, -3199);
			calendar.add(Calendar.YEAR, 9);
			AnniversaryDto ninthYear = new AnniversaryDto();
			ninthYear.setAnniversaryDate(df.format(calendar.getTime()));
			ninthYear.setAnniversaryTitle("9주년");
			ninthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 3300일
			calendar.add(Calendar.DATE, 3299);
			calendar.add(Calendar.YEAR, -9);
			AnniversaryDto threeThousandThreeHundred = new AnniversaryDto();
			threeThousandThreeHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousandThreeHundred.setAnniversaryTitle("3300일");
			threeThousandThreeHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 3400일 (days 후반부에 더 보여줄 데이터)
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto threeThousandFourHundred = new AnniversaryDto();
			threeThousandFourHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousandFourHundred.setAnniversaryTitle("3400일");
			threeThousandFourHundred.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 3000) {
				result.add(threeThousand);
				result.add(threeThousandOneHundred);
				result.add(threeThousandTwoHundred);
				result.add(ninthYear);
				result.add(threeThousandThreeHundred);
			} else if (days < 3100) {
				result.add(threeThousandOneHundred);
				result.add(threeThousandTwoHundred);
				result.add(ninthYear);
				result.add(threeThousandThreeHundred);
			} else if (days < 3200) {
				result.add(threeThousandTwoHundred);
				result.add(ninthYear);
				result.add(threeThousandThreeHundred);
				result.add(threeThousandFourHundred);
			} else {
				result.add(ninthYear);
				result.add(threeThousandThreeHundred);
				result.add(threeThousandFourHundred);
			}
//			System.out.println("days < 3285");
		}
		else if (days < 3650) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);

			// 3300일
			calendar.add(Calendar.DATE, 3299);
			AnniversaryDto threeThousandThreeHundred = new AnniversaryDto();
			threeThousandThreeHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousandThreeHundred.setAnniversaryTitle("3300일");
			threeThousandThreeHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 3400일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto threeThousandFourHundred = new AnniversaryDto();
			threeThousandFourHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousandFourHundred.setAnniversaryTitle("3400일");
			threeThousandFourHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 3500일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto threeThousandFiveHundred = new AnniversaryDto();
			threeThousandFiveHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousandFiveHundred.setAnniversaryTitle("3500일");
			threeThousandFiveHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 3600일
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto threeThousandSixHundred = new AnniversaryDto();
			threeThousandSixHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousandSixHundred.setAnniversaryTitle("3600일");
			threeThousandSixHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 10주년
			calendar.add(Calendar.DATE, -3599);
			calendar.add(Calendar.YEAR, 10);
			AnniversaryDto tenthYear = new AnniversaryDto();
			tenthYear.setAnniversaryDate(df.format(calendar.getTime()));
			tenthYear.setAnniversaryTitle("10주년");
			tenthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 3700일
			calendar.add(Calendar.DATE, 3699);
			calendar.add(Calendar.YEAR, -10);
			AnniversaryDto threeThousandSevenHundred = new AnniversaryDto();
			threeThousandSevenHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousandSevenHundred.setAnniversaryTitle("3700일");
			threeThousandSevenHundred.setRemainingDays(calcDate(calendar.getTime()));

			// 3800일 (days 후반부에 더 보여줄 데이터)
			calendar.add(Calendar.DATE, 100);
			AnniversaryDto threeThousandEightHundred = new AnniversaryDto();
			threeThousandEightHundred.setAnniversaryDate(df.format(calendar.getTime()));
			threeThousandEightHundred.setAnniversaryTitle("3800일");
			threeThousandEightHundred.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 3300) {
				result.add(threeThousandThreeHundred);
				result.add(threeThousandFourHundred);
				result.add(threeThousandFiveHundred);
				result.add(threeThousandSixHundred);
				result.add(tenthYear);
				result.add(threeThousandSevenHundred);
			} else if (days < 3400) {
				result.add(threeThousandFourHundred);
				result.add(threeThousandFiveHundred);
				result.add(threeThousandSixHundred);
				result.add(tenthYear);
				result.add(threeThousandSevenHundred);
			} else if (days < 3500) {
				result.add(threeThousandFiveHundred);
				result.add(threeThousandSixHundred);
				result.add(tenthYear);
				result.add(threeThousandSevenHundred);
			} else {
				result.add(threeThousandSixHundred);
				result.add(tenthYear);
				result.add(threeThousandSevenHundred);
				result.add(threeThousandEightHundred);
			}
//			System.out.println("days < 3650");
		}
		// 11년차 ~ 20년차
		else if (days > 3650 && days < 7300) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);

			// 4000일
			calendar.add(Calendar.DATE, 3999);
			AnniversaryDto fourThousand = new AnniversaryDto();
			fourThousand.setAnniversaryDate(df.format(calendar.getTime()));
			fourThousand.setAnniversaryTitle("4000일");
			fourThousand.setRemainingDays(calcDate(calendar.getTime()));

			// 11주년 4015
			calendar.add(Calendar.DATE, -3999);
			calendar.add(Calendar.YEAR, 11);
			AnniversaryDto eleventhYear = new AnniversaryDto();
			eleventhYear.setAnniversaryDate(df.format(calendar.getTime()));
			eleventhYear.setAnniversaryTitle("11주년");
			eleventhYear.setRemainingDays(calcDate(calendar.getTime()));

			// 12주년 4380
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto twelfthYear = new AnniversaryDto();
			twelfthYear.setAnniversaryDate(df.format(calendar.getTime()));
			twelfthYear.setAnniversaryTitle("12주년");
			twelfthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 13주년 4745
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto thirteenthYear = new AnniversaryDto();
			thirteenthYear.setAnniversaryDate(df.format(calendar.getTime()));
			thirteenthYear.setAnniversaryTitle("13주년");
			thirteenthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 5000일
			calendar.add(Calendar.DATE, 4999);
			calendar.add(Calendar.YEAR, -13);
			AnniversaryDto fiveThousand = new AnniversaryDto();
			fiveThousand.setAnniversaryDate(df.format(calendar.getTime()));
			fiveThousand.setAnniversaryTitle("5000일");
			fiveThousand.setRemainingDays(calcDate(calendar.getTime()));

			// 14주년 5110
			calendar.add(Calendar.DATE, -4999);
			calendar.add(Calendar.YEAR, 14);
			AnniversaryDto fourteenthYear = new AnniversaryDto();
			fourteenthYear.setAnniversaryDate(df.format(calendar.getTime()));
			fourteenthYear.setAnniversaryTitle("14주년");
			fourteenthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 15주년 5475
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto fifteenthYear = new AnniversaryDto();
			fifteenthYear.setAnniversaryDate(df.format(calendar.getTime()));
			fifteenthYear.setAnniversaryTitle("15주년");
			fifteenthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 16주년 5840
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto sixteenthYear = new AnniversaryDto();
			sixteenthYear.setAnniversaryDate(df.format(calendar.getTime()));
			sixteenthYear.setAnniversaryTitle("16주년");
			sixteenthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 6000일
			calendar.add(Calendar.DATE, 5999);
			calendar.add(Calendar.YEAR, -16);
			AnniversaryDto sixThousand = new AnniversaryDto();
			sixThousand.setAnniversaryDate(df.format(calendar.getTime()));
			sixThousand.setAnniversaryTitle("6000일");
			sixThousand.setRemainingDays(calcDate(calendar.getTime()));

			// 17주년 6205
			calendar.add(Calendar.DATE, -5999);
			calendar.add(Calendar.YEAR, 17);
			AnniversaryDto seventeenthYear = new AnniversaryDto();
			seventeenthYear.setAnniversaryDate(df.format(calendar.getTime()));
			seventeenthYear.setAnniversaryTitle("17주년");
			seventeenthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 18주년 6570
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto eighteenthYear = new AnniversaryDto();
			eighteenthYear.setAnniversaryDate(df.format(calendar.getTime()));
			eighteenthYear.setAnniversaryTitle("18주년");
			eighteenthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 19주년 6935
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto nineteenthYear = new AnniversaryDto();
			nineteenthYear.setAnniversaryDate(df.format(calendar.getTime()));
			nineteenthYear.setAnniversaryTitle("19주년");
			nineteenthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 7000일
			calendar.add(Calendar.DATE, 6999);
			calendar.add(Calendar.YEAR, -19);
			AnniversaryDto sevenThousand = new AnniversaryDto();
			sevenThousand.setAnniversaryDate(df.format(calendar.getTime()));
			sevenThousand.setAnniversaryTitle("7000일");
			sevenThousand.setRemainingDays(calcDate(calendar.getTime()));

			// 20주년 7300
			calendar.add(Calendar.DATE, -6999);
			calendar.add(Calendar.YEAR, 20);
			AnniversaryDto twentiethYear = new AnniversaryDto();
			twentiethYear.setAnniversaryDate(df.format(calendar.getTime()));
			twentiethYear.setAnniversaryTitle("20주년");
			twentiethYear.setRemainingDays(calcDate(calendar.getTime()));

			// 8000일
			calendar.add(Calendar.DATE, 7999);
			calendar.add(Calendar.YEAR, -20);
			AnniversaryDto eightThousand = new AnniversaryDto();
			eightThousand.setAnniversaryDate(df.format(calendar.getTime()));
			eightThousand.setAnniversaryTitle("8000일");
			eightThousand.setRemainingDays(calcDate(calendar.getTime()));

			// 21주년
			calendar.add(Calendar.DATE, -7999);
			calendar.add(Calendar.YEAR, 21);
			AnniversaryDto twentyFirstYear = new AnniversaryDto();
			twentyFirstYear.setAnniversaryDate(df.format(calendar.getTime()));
			twentyFirstYear.setAnniversaryTitle("21주년");
			twentyFirstYear.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 4000) {
				result.add(fourThousand);
				result.add(eleventhYear);
				result.add(twelfthYear);
				result.add(thirteenthYear);
				result.add(fiveThousand);
				result.add(fourteenthYear);
				result.add(fifteenthYear);
				result.add(sixteenthYear);
				result.add(sixThousand);
				result.add(seventeenthYear);
				result.add(eighteenthYear);
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 4015) {
				result.add(eleventhYear);
				result.add(twelfthYear);
				result.add(thirteenthYear);
				result.add(fiveThousand);
				result.add(fourteenthYear);
				result.add(fifteenthYear);
				result.add(sixteenthYear);
				result.add(sixThousand);
				result.add(seventeenthYear);
				result.add(eighteenthYear);
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 4380) {
				result.add(twelfthYear);
				result.add(thirteenthYear);
				result.add(fiveThousand);
				result.add(fourteenthYear);
				result.add(fifteenthYear);
				result.add(sixteenthYear);
				result.add(sixThousand);
				result.add(seventeenthYear);
				result.add(eighteenthYear);
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 4745) {
				result.add(thirteenthYear);
				result.add(fiveThousand);
				result.add(fourteenthYear);
				result.add(fifteenthYear);
				result.add(sixteenthYear);
				result.add(sixThousand);
				result.add(seventeenthYear);
				result.add(eighteenthYear);
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 5000) {
				result.add(fiveThousand);
				result.add(fourteenthYear);
				result.add(fifteenthYear);
				result.add(sixteenthYear);
				result.add(sixThousand);
				result.add(seventeenthYear);
				result.add(eighteenthYear);
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 5110) {
				result.add(fourteenthYear);
				result.add(fifteenthYear);
				result.add(sixteenthYear);
				result.add(sixThousand);
				result.add(seventeenthYear);
				result.add(eighteenthYear);
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 5475) {
				result.add(fifteenthYear);
				result.add(sixteenthYear);
				result.add(sixThousand);
				result.add(seventeenthYear);
				result.add(eighteenthYear);
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 5840) {
				result.add(sixteenthYear);
				result.add(sixThousand);
				result.add(seventeenthYear);
				result.add(eighteenthYear);
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 6000) {
				result.add(sixThousand);
				result.add(seventeenthYear);
				result.add(eighteenthYear);
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 6205) {
				result.add(seventeenthYear);
				result.add(eighteenthYear);
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 6570) {
				result.add(eighteenthYear);
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 6935) {
				result.add(nineteenthYear);
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else if (days < 7000) {
				result.add(sevenThousand);
				result.add(twentiethYear);
				result.add(eightThousand);
			} else {
				result.add(twentiethYear);
				result.add(eightThousand);
				result.add(twentyFirstYear);
			}
		}
		else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(coupleRegDt);

			// 21주년
			calendar.add(Calendar.YEAR, 21);
			AnniversaryDto twentyFirstYear = new AnniversaryDto();
			twentyFirstYear.setAnniversaryDate(df.format(calendar.getTime()));
			twentyFirstYear.setAnniversaryTitle("21주년");
			twentyFirstYear.setRemainingDays(calcDate(calendar.getTime()));

			// 22주년
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto twentySecondYear = new AnniversaryDto();
			twentySecondYear.setAnniversaryDate(df.format(calendar.getTime()));
			twentySecondYear.setAnniversaryTitle("22주년");
			twentySecondYear.setRemainingDays(calcDate(calendar.getTime()));

			// 23주년
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto twentyThirdYear = new AnniversaryDto();
			twentyThirdYear.setAnniversaryDate(df.format(calendar.getTime()));
			twentyThirdYear.setAnniversaryTitle("23주년");
			twentyThirdYear.setRemainingDays(calcDate(calendar.getTime()));

			// 24주년
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto twentyFourthYear = new AnniversaryDto();
			twentyFourthYear.setAnniversaryDate(df.format(calendar.getTime()));
			twentyFourthYear.setAnniversaryTitle("24주년");
			twentyFourthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 25주년
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto twentyFifthYear = new AnniversaryDto();
			twentyFifthYear.setAnniversaryDate(df.format(calendar.getTime()));
			twentyFifthYear.setAnniversaryTitle("25주년");
			twentyFifthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 26주년
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto twentySixthYear = new AnniversaryDto();
			twentySixthYear.setAnniversaryDate(df.format(calendar.getTime()));
			twentySixthYear.setAnniversaryTitle("26주년");
			twentySixthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 27주년
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto twentySeventhYear = new AnniversaryDto();
			twentySeventhYear.setAnniversaryDate(df.format(calendar.getTime()));
			twentySeventhYear.setAnniversaryTitle("27주년");
			twentySeventhYear.setRemainingDays(calcDate(calendar.getTime()));

			// 28주년
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto twentyEighthYear = new AnniversaryDto();
			twentyEighthYear.setAnniversaryDate(df.format(calendar.getTime()));
			twentyEighthYear.setAnniversaryTitle("28주년");
			twentyEighthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 29주년
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto twentyNinthYear = new AnniversaryDto();
			twentyNinthYear.setAnniversaryDate(df.format(calendar.getTime()));
			twentyNinthYear.setAnniversaryTitle("29주년");
			twentyNinthYear.setRemainingDays(calcDate(calendar.getTime()));

			// 30주년
			calendar.add(Calendar.YEAR, 1);
			AnniversaryDto thirtiethYear = new AnniversaryDto();
			thirtiethYear.setAnniversaryDate(df.format(calendar.getTime()));
			thirtiethYear.setAnniversaryTitle("30주년");
			thirtiethYear.setRemainingDays(calcDate(calendar.getTime()));

			// 40주년
			calendar.add(Calendar.YEAR, 10);
			AnniversaryDto fortiethYear = new AnniversaryDto();
			fortiethYear.setAnniversaryDate(df.format(calendar.getTime()));
			fortiethYear.setAnniversaryTitle("40주년");
			fortiethYear.setRemainingDays(calcDate(calendar.getTime()));

			// 50주년
			calendar.add(Calendar.YEAR, 10);
			AnniversaryDto fiftiethYear = new AnniversaryDto();
			fiftiethYear.setAnniversaryDate(df.format(calendar.getTime()));
			fiftiethYear.setAnniversaryTitle("50주년");
			fiftiethYear.setRemainingDays(calcDate(calendar.getTime()));

			if (days < 7665) {
				result.add(twentyFirstYear);
				result.add(twentySecondYear);
				result.add(twentyThirdYear);
				result.add(twentyFourthYear);
				result.add(twentyFifthYear);
				result.add(twentySixthYear);
				result.add(twentySeventhYear);
				result.add(twentyEighthYear);
				result.add(twentyNinthYear);
				result.add(thirtiethYear);
				result.add(fortiethYear);
				result.add(fiftiethYear);
			} else if (days < 8030) {
				result.add(twentySecondYear);
				result.add(twentyThirdYear);
				result.add(twentyFourthYear);
				result.add(twentyFifthYear);
				result.add(twentySixthYear);
				result.add(twentySeventhYear);
				result.add(twentyEighthYear);
				result.add(twentyNinthYear);
				result.add(thirtiethYear);
				result.add(fortiethYear);
				result.add(fiftiethYear);
			} else if (days < 8395) {
				result.add(twentyThirdYear);
				result.add(twentyFourthYear);
				result.add(twentyFifthYear);
				result.add(twentySixthYear);
				result.add(twentySeventhYear);
				result.add(twentyEighthYear);
				result.add(twentyNinthYear);
				result.add(thirtiethYear);
				result.add(fortiethYear);
				result.add(fiftiethYear);
			} else if (days < 8760) {
				result.add(twentyFourthYear);
				result.add(twentyFifthYear);
				result.add(twentySixthYear);
				result.add(twentySeventhYear);
				result.add(twentyEighthYear);
				result.add(twentyNinthYear);
				result.add(thirtiethYear);
				result.add(fortiethYear);
				result.add(fiftiethYear);
			} else if (days < 9125) {
				result.add(twentyFifthYear);
				result.add(twentySixthYear);
				result.add(twentySeventhYear);
				result.add(twentyEighthYear);
				result.add(twentyNinthYear);
				result.add(thirtiethYear);
				result.add(fortiethYear);
				result.add(fiftiethYear);
			} else if (days < 9490) {
				result.add(twentySixthYear);
				result.add(twentySeventhYear);
				result.add(twentyEighthYear);
				result.add(twentyNinthYear);
				result.add(thirtiethYear);
				result.add(fortiethYear);
				result.add(fiftiethYear);
			} else if (days < 9855) {
				result.add(twentySeventhYear);
				result.add(twentyEighthYear);
				result.add(twentyNinthYear);
				result.add(thirtiethYear);
				result.add(fortiethYear);
				result.add(fiftiethYear);
			} else if (days < 10220) {
				result.add(twentyEighthYear);
				result.add(twentyNinthYear);
				result.add(thirtiethYear);
				result.add(fortiethYear);
				result.add(fiftiethYear);
			} else if (days < 10585) {
				result.add(twentyNinthYear);
				result.add(thirtiethYear);
				result.add(fortiethYear);
				result.add(fiftiethYear);
			} else if (days < 10950) {
				result.add(thirtiethYear);
				result.add(fortiethYear);
				result.add(fiftiethYear);
			} else if (days < 14600) {
				result.add(fortiethYear);
				result.add(fiftiethYear);
			} else {
				result.add(fiftiethYear);
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
			} else {
				return "D" + (int) sub;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

}
