package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.calendar.MenstrualCycleCalendarDto;
import com.msm.heartnal.core.dto.calendar.MenstrualCycleDto;
import com.msm.heartnal.core.dto.calendar.MenstrualCycleMessageDto;
import com.msm.heartnal.core.enums.Contraceptive;
import com.msm.heartnal.core.enums.MenstrualCycle;
import com.msm.heartnal.core.enums.MenstrualCycleMessage;
import com.msm.heartnal.core.mapper.CalendarMapper;
import com.msm.heartnal.core.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * @author 최대희
 * @since 2022-08-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {
	private final CalendarMapper calendarMapper;
	private final MemberMapper memberMapper;

	@Override
	public boolean setMenstrualCycle(MenstrualCycleDto menstrualCycleDto) {
		MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(menstrualCycleDto.getMemberSeq());
		menstrualCycleDto.setCoupleMemberSeq(memberDao.getCoupleMemberSeq());

		MenstrualCycleDto myData = calendarMapper.getMenstrualCycle(menstrualCycleDto.getMemberSeq(), menstrualCycleDto.getCoupleCode());

		if (myData == null) {
			// 만약 DB Table 에 insert 가 되어있지 않으면 insert 먼저 해줘야함
			MenstrualCycleDto insertData = new MenstrualCycleDto();
			insertData.setMemberSeq(menstrualCycleDto.getMemberSeq());
			insertData.setCoupleMemberSeq(memberDao.getCoupleMemberSeq());
			insertData.setCoupleCode(menstrualCycleDto.getCoupleCode());
			insertData.setContraceptiveYN("N");

			boolean insertResult = calendarMapper.setMenstrualCycle(insertData);

			menstrualCycleDto.setMenstrualCycleSeq(insertData.getMenstrualCycleSeq());
			menstrualCycleDto.setContraceptiveYN(insertData.getContraceptiveYN());

			if (insertResult) {
				return calendarMapper.updateMenstrualCycle(menstrualCycleDto);
			} else {
				return false;
			}
		} else {
			// 이미 기본 데이터는 있으니까 update 해주면 됨

			if (menstrualCycleDto.getLastMenstrualStartDt() != null) {
				myData.setLastMenstrualStartDt(menstrualCycleDto.getLastMenstrualStartDt());
			}
			if (menstrualCycleDto.getMenstrualCycle() != null) {
				myData.setMenstrualCycle(menstrualCycleDto.getMenstrualCycle());
			}
			if (menstrualCycleDto.getMenstrualPeriod() != null) {
				myData.setMenstrualPeriod(menstrualCycleDto.getMenstrualPeriod());
			}
			if (menstrualCycleDto.getContraceptiveYN() != null) {
				myData.setContraceptiveYN(menstrualCycleDto.getContraceptiveYN());
			}
			if (menstrualCycleDto.getTakingContraceptiveDt() != null) {
				myData.setTakingContraceptiveDt(menstrualCycleDto.getTakingContraceptiveDt());
			}
			if (menstrualCycleDto.getContraceptive() != null && menstrualCycleDto.getContraceptive() != Contraceptive.CONTRACEPTIVE_NONE) {
				myData.setContraceptiveYN("Y");
				myData.setContraceptive(menstrualCycleDto.getContraceptive());
			} else {
				myData.setContraceptiveYN("N");
				myData.setContraceptive(menstrualCycleDto.getContraceptive());
			}
			// 알람은 여기서 할 필요가 없음, 메시지 설정할 때 같이 바꿔줘야함

			return calendarMapper.updateMenstrualCycle(myData);
		}
	}

	@Override
	public MenstrualCycleDto getMenstrualCycle(Long memberSeq, String coupleCode) {
		return calendarMapper.getMenstrualCycle(memberSeq, coupleCode);
	}

	@Override
	public boolean permissionCheck(Long memberSeq, String coupleCode) {
		// null 이거나 memberSeq == 가져온 데이터 memberSeq 가 같다면 true 아니면 false
		MenstrualCycleDto myData = calendarMapper.getMenstrualCycle(memberSeq, coupleCode);

		if (myData == null || Objects.equals(myData.getMemberSeq(), memberSeq)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean updateMenstrualCycle(MenstrualCycleDto menstrualCycleDto) {
		return calendarMapper.updateMenstrualCycle(menstrualCycleDto);
	}

	@Override
	public boolean deleteMenstrualCycle(MenstrualCycleDto menstrualCycleDto) {
		return calendarMapper.deleteMenstrualCycle(menstrualCycleDto);
	}

	@Override
	@Transactional(rollbackFor = {RuntimeException.class, Error.class})
	public boolean initMenstrualCycle(Long memberSeq, String coupleCode) {
		MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(memberSeq);

		MenstrualCycleDto menstrualCycleDto = new MenstrualCycleDto();
		menstrualCycleDto.setMemberSeq(memberSeq);
		menstrualCycleDto.setCoupleCode(coupleCode);

		MenstrualCycleMessageDto menstrualCycleMessageDto = new MenstrualCycleMessageDto();
		menstrualCycleMessageDto.setMemberSeq(memberDao.getCoupleMemberSeq());
		menstrualCycleMessageDto.setCoupleCode(coupleCode);

		MenstrualCycleDto menstrualCycleData = calendarMapper.getMenstrualCycle(memberSeq, coupleCode);
		MenstrualCycleMessageDto menstrualCycleMessageData = calendarMapper.getMenstrualCycleMessage(memberSeq, coupleCode);

		try {
			if (menstrualCycleData == null) {
				return false;
			} else {
				if (menstrualCycleMessageData == null) {
					return calendarMapper.deleteMenstrualCycle(menstrualCycleDto);
				} else {
					boolean menstrualCycleResult = calendarMapper.deleteMenstrualCycle(menstrualCycleDto);
					boolean menstrualCycleMessageResult = calendarMapper.deleteMenstrualCycleMessage(menstrualCycleMessageDto);

					return menstrualCycleResult && menstrualCycleMessageResult;
				}
			}
		} catch (Exception e) {
			log.error("initMenstrualCycle Error!!!");
			log.error(e.toString());
			return false;
		}
	}

	@Override
	public boolean setMenstrualCycleMessage(MenstrualCycleMessageDto menstrualCycleMessageDto) {
		MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(menstrualCycleMessageDto.getMemberSeq());
		menstrualCycleMessageDto.setCoupleMemberSeq(memberDao.getCoupleMemberSeq());

		MenstrualCycleMessageDto myData = calendarMapper.getMenstrualCycleMessage(menstrualCycleMessageDto.getMemberSeq(), menstrualCycleMessageDto.getCoupleCode());

		if (myData == null) {
			MenstrualCycleMessageDto insertData = new MenstrualCycleMessageDto();
			insertData.setMemberSeq(menstrualCycleMessageDto.getMemberSeq());
			insertData.setCoupleMemberSeq(memberDao.getCoupleMemberSeq());
			insertData.setCoupleCode(menstrualCycleMessageDto.getCoupleCode());
			// 추가로 여기는 기본 메시지들을 저장해줘야함
			insertData.setMenstruation3DaysAgoAlarm("N");
			insertData.setMenstruation3DaysAgo(MenstrualCycleMessage.MENSTRUATION_3DAYS_AGO.getText());
			insertData.setMenstruationDtAlarm("N");
			insertData.setMenstruationDt(MenstrualCycleMessage.MENSTRUATION_DAY.getText());
			insertData.setOvulationDtAlarm("N");
			insertData.setOvulationDt(MenstrualCycleMessage.OVULATION_DAY.getText());
			insertData.setFertileWindowStartDtAlarm("N");
			insertData.setFertileWindowStartDt(MenstrualCycleMessage.FERTILE_WINDOW_START_DATE.getText());
			insertData.setFertileWindowsEndDtAlarm("N");
			insertData.setFertileWindowsEndDt(MenstrualCycleMessage.FERTILE_WINDOW_END_DATE.getText());

			boolean insertResult = calendarMapper.setMenstrualCycleMessage(insertData);

			if (insertResult) {
				menstrualCycleMessageDto.setMenstrualCycleMessageSeq(insertData.getMenstrualCycleMessageSeq());
				if (menstrualCycleMessageDto.getMenstruation3DaysAgo() == null) {
					menstrualCycleMessageDto.setMenstruation3DaysAgoAlarm("N");
					menstrualCycleMessageDto.setMenstruation3DaysAgo(insertData.getMenstruation3DaysAgo());
				}
				if (menstrualCycleMessageDto.getMenstruationDt() == null) {
					menstrualCycleMessageDto.setMenstruationDtAlarm("N");
					menstrualCycleMessageDto.setMenstruationDt(insertData.getMenstruationDt());
				}
				if (menstrualCycleMessageDto.getOvulationDt() == null) {
					menstrualCycleMessageDto.setOvulationDtAlarm("N");
					menstrualCycleMessageDto.setOvulationDt(insertData.getOvulationDt());
				}
				if (menstrualCycleMessageDto.getFertileWindowStartDt() == null) {
					menstrualCycleMessageDto.setFertileWindowStartDtAlarm("N");
					menstrualCycleMessageDto.setFertileWindowStartDt(insertData.getFertileWindowStartDt());
				}
				if (menstrualCycleMessageDto.getFertileWindowsEndDt() == null) {
					menstrualCycleMessageDto.setFertileWindowsEndDtAlarm("N");
					menstrualCycleMessageDto.setFertileWindowsEndDt(insertData.getFertileWindowsEndDt());
				}

				return calendarMapper.updateMenstrualCycleMessage(menstrualCycleMessageDto);
			} else {
				return false;
			}
		} else {
			if (menstrualCycleMessageDto.getMenstruation3DaysAgo() != null) {
				myData.setMenstruation3DaysAgoAlarm(menstrualCycleMessageDto.getMenstruation3DaysAgoAlarm());
				myData.setMenstruation3DaysAgo(menstrualCycleMessageDto.getMenstruation3DaysAgo());
			}
			if (menstrualCycleMessageDto.getMenstruationDt() != null) {
				myData.setMenstruationDtAlarm(menstrualCycleMessageDto.getMenstruationDtAlarm());
				myData.setMenstruationDt(menstrualCycleMessageDto.getMenstruationDt());
			}
			if (menstrualCycleMessageDto.getOvulationDt() != null) {
				myData.setOvulationDtAlarm(menstrualCycleMessageDto.getOvulationDtAlarm());
				myData.setOvulationDt(menstrualCycleMessageDto.getOvulationDt());
			}
			if (menstrualCycleMessageDto.getFertileWindowStartDt() != null) {
				myData.setFertileWindowStartDtAlarm(menstrualCycleMessageDto.getFertileWindowStartDtAlarm());
				myData.setFertileWindowStartDt(menstrualCycleMessageDto.getFertileWindowStartDt());
			}
			if (menstrualCycleMessageDto.getFertileWindowsEndDt() != null) {
				myData.setFertileWindowsEndDtAlarm(menstrualCycleMessageDto.getFertileWindowsEndDtAlarm());
				myData.setFertileWindowsEndDt(menstrualCycleMessageDto.getFertileWindowsEndDt());
			}
			return calendarMapper.updateMenstrualCycleMessage(myData);
		}
	}

	@Override
	public boolean setCoupleMenstrualCycleMessage(MenstrualCycleMessageDto menstrualCycleMessageDto) {
		MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(menstrualCycleMessageDto.getMemberSeq());
		menstrualCycleMessageDto.setMemberSeq(memberDao.getCoupleMemberSeq());
		menstrualCycleMessageDto.setCoupleMemberSeq(memberDao.getMemberSeq());

		MenstrualCycleMessageDto myData = calendarMapper.getMenstrualCycleMessage(menstrualCycleMessageDto.getMemberSeq(), menstrualCycleMessageDto.getCoupleCode());

		if (myData == null) {
			MenstrualCycleMessageDto insertData = new MenstrualCycleMessageDto();
			insertData.setMemberSeq(memberDao.getCoupleMemberSeq());
			insertData.setCoupleMemberSeq(memberDao.getMemberSeq());
			insertData.setCoupleCode(menstrualCycleMessageDto.getCoupleCode());
			// 추가로 여기는 기본 메시지들을 저장해줘야함
			insertData.setMenstruation3DaysAgoAlarm("N");
			insertData.setMenstruation3DaysAgo(MenstrualCycleMessage.MENSTRUATION_3DAYS_AGO.getText());
			insertData.setMenstruationDtAlarm("N");
			insertData.setMenstruationDt(MenstrualCycleMessage.MENSTRUATION_DAY.getText());
			insertData.setOvulationDtAlarm("N");
			insertData.setOvulationDt(MenstrualCycleMessage.OVULATION_DAY.getText());
			insertData.setFertileWindowStartDtAlarm("N");
			insertData.setFertileWindowStartDt(MenstrualCycleMessage.FERTILE_WINDOW_START_DATE.getText());
			insertData.setFertileWindowsEndDtAlarm("N");
			insertData.setFertileWindowsEndDt(MenstrualCycleMessage.FERTILE_WINDOW_END_DATE.getText());

			boolean insertResult = calendarMapper.setMenstrualCycleMessage(insertData);

			if (insertResult) {
				menstrualCycleMessageDto.setMenstrualCycleMessageSeq(insertData.getMenstrualCycleMessageSeq());
				if (menstrualCycleMessageDto.getMenstruation3DaysAgo() == null) {
					menstrualCycleMessageDto.setMenstruation3DaysAgoAlarm("N");
					menstrualCycleMessageDto.setMenstruation3DaysAgo(insertData.getMenstruation3DaysAgo());
				}
				if (menstrualCycleMessageDto.getMenstruationDt() == null) {
					menstrualCycleMessageDto.setMenstruationDtAlarm("N");
					menstrualCycleMessageDto.setMenstruationDt(insertData.getMenstruationDt());
				}
				if (menstrualCycleMessageDto.getOvulationDt() == null) {
					menstrualCycleMessageDto.setOvulationDtAlarm("N");
					menstrualCycleMessageDto.setOvulationDt(insertData.getOvulationDt());
				}
				if (menstrualCycleMessageDto.getFertileWindowStartDt() == null) {
					menstrualCycleMessageDto.setFertileWindowStartDtAlarm("N");
					menstrualCycleMessageDto.setFertileWindowStartDt(insertData.getFertileWindowStartDt());
				}
				if (menstrualCycleMessageDto.getFertileWindowsEndDt() == null) {
					menstrualCycleMessageDto.setFertileWindowsEndDtAlarm("N");
					menstrualCycleMessageDto.setFertileWindowsEndDt(insertData.getFertileWindowsEndDt());
				}

				return calendarMapper.updateMenstrualCycleMessage(menstrualCycleMessageDto);
			} else {
				return false;
			}
		} else {
			if (menstrualCycleMessageDto.getMenstruation3DaysAgo() != null) {
				myData.setMenstruation3DaysAgoAlarm(menstrualCycleMessageDto.getMenstruation3DaysAgoAlarm());
				myData.setMenstruation3DaysAgo(menstrualCycleMessageDto.getMenstruation3DaysAgo());
			}
			if (menstrualCycleMessageDto.getMenstruationDt() != null) {
				myData.setMenstruationDtAlarm(menstrualCycleMessageDto.getMenstruationDtAlarm());
				myData.setMenstruationDt(menstrualCycleMessageDto.getMenstruationDt());
			}
			if (menstrualCycleMessageDto.getOvulationDt() != null) {
				myData.setOvulationDtAlarm(menstrualCycleMessageDto.getOvulationDtAlarm());
				myData.setOvulationDt(menstrualCycleMessageDto.getOvulationDt());
			}
			if (menstrualCycleMessageDto.getFertileWindowStartDt() != null) {
				myData.setFertileWindowStartDtAlarm(menstrualCycleMessageDto.getFertileWindowStartDtAlarm());
				myData.setFertileWindowStartDt(menstrualCycleMessageDto.getFertileWindowStartDt());
			}
			if (menstrualCycleMessageDto.getFertileWindowsEndDt() != null) {
				myData.setFertileWindowsEndDtAlarm(menstrualCycleMessageDto.getFertileWindowsEndDtAlarm());
				myData.setFertileWindowsEndDt(menstrualCycleMessageDto.getFertileWindowsEndDt());
			}
			return calendarMapper.updateMenstrualCycleMessage(myData);
		}
	}

	@Override
	public MenstrualCycleMessageDto getMenstrualCycleMessage(Long memberSeq, String coupleCode) {
		return calendarMapper.getMenstrualCycleMessage(memberSeq, coupleCode);
	}

	@Override
	public MenstrualCycleMessageDto getMenstrualCycleCoupleMessage(Long memberSeq, String coupleCode) {
		MemberDao memberDao = memberMapper.getMemberInfoBySenderMemberSeq(memberSeq);

		return calendarMapper.getMenstrualCycleMessage(memberDao.getCoupleMemberSeq(), coupleCode);
	}

	@Override
	public boolean updateMenstrualCycleMessage(MenstrualCycleMessageDto menstrualCycleMessageDto) {
		return calendarMapper.updateMenstrualCycleMessage(menstrualCycleMessageDto);
	}

	@Override
	public boolean deleteMenstrualCycleMessage(MenstrualCycleMessageDto menstrualCycleMessageDto) {
		return calendarMapper.deleteMenstrualCycleMessage(menstrualCycleMessageDto);
	}

	@Override
	public MenstrualCycleCalendarDto getMenstrualCycleCalendar(Long memberSeq, String coupleCode) throws ParseException {
		MenstrualCycleDto menstrualCycleDto = calendarMapper.getMenstrualCycle(memberSeq, coupleCode);

		// 데이터가 있는지 없는지부터 판단해야함
		if (menstrualCycleDto == null || menstrualCycleDto.getLastMenstrualStartDt() == null || menstrualCycleDto.getMenstrualCycle() == null || menstrualCycleDto.getMenstrualPeriod() == null) {
			MenstrualCycleCalendarDto result = new MenstrualCycleCalendarDto();
			result.setValid(false);
			return result;
		} else {
			int takingContraceptiveCycle = 0;

			// 일단 피임약 복용 여부에 상관없이 생리주기를 먼저 생성해야함. 그래야 이 후 피임약 복용 여부에 따라 다른 생리주기 생성 가능

			// 생리주기의 경우 이번달을 기준으로 해야함 만약 이전 생리 예전 날짜가 현재보다 -1달이 아닌 경우 그 이상의 계산을 해줘야함
			int todayMonth = LocalDate.now().getMonthValue();
			int lastMenstrualStartDtMonth = LocalDate.parse(menstrualCycleDto.getLastMenstrualStartDt().substring(0, 10)).getMonthValue();

			SimpleDateFormat sDate = new SimpleDateFormat("yyyy-MM-dd");
			Date startDt = sDate.parse(menstrualCycleDto.getLastMenstrualStartDt());
			Calendar menstrualCyclePeriodCalender = Calendar.getInstance();
			menstrualCyclePeriodCalender.setTime(startDt);

			LocalDate today = LocalDate.now();
			LocalDate menstrualDt = LocalDate.parse(menstrualCycleDto.getLastMenstrualStartDt().substring(0, 10)).plusDays(menstrualCycleDto.getMenstrualCycle() * 2);

			// 차이가 1이상이면 여기서 자체적으로 lastMenstrualStartDtMonth 를 만들어줘서 미리 업데이트 후 생리주기를 생성하는게 맞음
			if ((todayMonth - lastMenstrualStartDtMonth) > 1 || today.isAfter(menstrualDt)) {
				MenstrualCycleCalendarDto menstrualCycleCalendarDto = new MenstrualCycleCalendarDto();
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date lastMenstrualStartDt = format.parse(menstrualCycleDto.getLastMenstrualStartDt());
				Calendar lastMenstrualCalendar = Calendar.getInstance();
				lastMenstrualCalendar.setTime(startDt);

				// 년도가 달라졌을 때를 대비하여 Math.abs 함수를 통해 값을 절대값으로 바꿔줌
				for (int i = 0; i < Math.abs((todayMonth - lastMenstrualStartDtMonth)); i++) {
					lastMenstrualCalendar.add(Calendar.DATE, menstrualCycleDto.getMenstrualCycle());
				}

				MenstrualCycleDto updateData = new MenstrualCycleDto();
				updateData.setMenstrualCycleSeq(menstrualCycleDto.getMenstrualCycleSeq());
				updateData.setMemberSeq(menstrualCycleDto.getMemberSeq());
				updateData.setLastMenstrualStartDt(format.format(lastMenstrualCalendar.getTime()));

				calendarMapper.updateLastMenstrualStartDt(updateData);

				MenstrualCycleDto afterMenstrualCycleDto = calendarMapper.getMenstrualCycle(memberSeq, coupleCode);

				MenstrualCycleCalendarDto afterResult = new MenstrualCycleCalendarDto();
				MenstrualCycleCalendarDto menstrualCycleResult = getMenstrualCyclePeriod(afterMenstrualCycleDto.getLastMenstrualStartDt(), afterMenstrualCycleDto.getMenstrualCycle(), afterMenstrualCycleDto.getMenstrualPeriod(), takingContraceptiveCycle);
				MenstrualCycleCalendarDto ovulationDateResult = getOvulationDt(menstrualCycleResult);
				MenstrualCycleCalendarDto fertileWindowDtResult  = getFertileWindowDt(ovulationDateResult);

				afterResult = fertileWindowDtResult;

				if (Objects.equals(menstrualCycleDto.getContraceptiveYN(), "Y") && menstrualCycleDto.getTakingContraceptiveDt() != null) {
					return getContraceptiveMenstrualDate(menstrualCycleDto.getContraceptive(), afterResult, menstrualCycleDto.getTakingContraceptiveDt(), menstrualCycleDto.getMenstrualCycle(), menstrualCycleDto.getMenstrualPeriod());
				} else {
					afterResult.setValid(true);
					return afterResult;
				}

			} else {
				MenstrualCycleCalendarDto result = new MenstrualCycleCalendarDto();
				MenstrualCycleCalendarDto menstrualCycleResult = getMenstrualCyclePeriod(menstrualCycleDto.getLastMenstrualStartDt(), menstrualCycleDto.getMenstrualCycle(), menstrualCycleDto.getMenstrualPeriod(), takingContraceptiveCycle);
				MenstrualCycleCalendarDto ovulationDateResult = getOvulationDt(menstrualCycleResult);
				MenstrualCycleCalendarDto fertileWindowDtResult  = getFertileWindowDt(ovulationDateResult);

				result = fertileWindowDtResult;

				if (Objects.equals(menstrualCycleDto.getContraceptiveYN(), "Y") && menstrualCycleDto.getTakingContraceptiveDt() != null) {
					// 피임약 복용이 N 인 경우 처음에 만든 생리주기
					return getContraceptiveMenstrualDate(menstrualCycleDto.getContraceptive(), result, menstrualCycleDto.getTakingContraceptiveDt(), menstrualCycleDto.getMenstrualCycle(), menstrualCycleDto.getMenstrualPeriod());
				} else {
					return result;
				}
			}
		}
		// 피임약 복용여부 및 피임약 복용 날짜 그리고 피임약 종류에 따라 생리 주기 반영해줘야함
		// 피임약은 생리 시작날 먹는게 가장 효과적이며, 최대 생리시작 5일까지 복용하면 효과 있음, 다만 1일째가 아니면 콘돔등의 추가적인 피임방법을 병행해야하고 생리가 끝나고 먹으면 의미 X
	}

	// 생리주기 구하기
	public static MenstrualCycleCalendarDto getMenstrualCyclePeriod(String previousMenstrualDt, int menstrualCycle, int menstrualPeriod, int takingContraceptiveCycle) throws ParseException {

		MenstrualCycleCalendarDto menstrualCycleCalendarDto = new MenstrualCycleCalendarDto();
		SimpleDateFormat sDate = new SimpleDateFormat("yyyy-MM-dd");
		Date startDt = sDate.parse(previousMenstrualDt);
		Calendar menstrualCyclePeriodCalender = Calendar.getInstance();
		menstrualCyclePeriodCalender.setTime(startDt);

		if (takingContraceptiveCycle == 0) {
			menstrualCyclePeriodCalender.add(Calendar.DATE, menstrualCycle);
			menstrualCycleCalendarDto.setMenstrualCycleStartDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			// 여기서 생리 마지막날 더해줄때 -1 해줘야함 왜냐하면 첫날 포함이기 때문에
			menstrualCyclePeriodCalender.add(Calendar.DATE, menstrualPeriod -1);
			menstrualCycleCalendarDto.setMenstrualCycleEndDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			menstrualCycleCalendarDto.setMenstrualCycleMemo(MenstrualCycle.MENSTRUAL_CYCLE.getText());

			menstrualCyclePeriodCalender.add(Calendar.DATE, menstrualCycle);
			menstrualCyclePeriodCalender.add(Calendar.DATE, -(menstrualPeriod -1));
			menstrualCycleCalendarDto.setAfterMenstrualCycleStartDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			menstrualCyclePeriodCalender.add(Calendar.DATE, menstrualPeriod -1);
			menstrualCycleCalendarDto.setAfterMenstrualCycleEndDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			menstrualCycleCalendarDto.setAfterMenstrualCycleMemo(MenstrualCycle.MENSTRUAL_CYCLE.getText());

			menstrualCyclePeriodCalender.add(Calendar.DATE, -(menstrualPeriod -1));
			menstrualCyclePeriodCalender.add(Calendar.DATE, -menstrualCycle);
			menstrualCyclePeriodCalender.add(Calendar.DATE, -menstrualCycle);

			menstrualCycleCalendarDto.setBeforeMenstrualCycleStartDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			menstrualCyclePeriodCalender.add(Calendar.DATE, menstrualPeriod -1);
			menstrualCycleCalendarDto.setBeforeMenstrualCycleEndDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			menstrualCycleCalendarDto.setBeforeMenstrualCycleMemo(MenstrualCycle.MENSTRUAL_CYCLE.getText());
		} else {
			menstrualCyclePeriodCalender.add(Calendar.DATE, takingContraceptiveCycle);
			menstrualCycleCalendarDto.setMenstrualCycleStartDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			// 여기서 생리 마지막날 더해줄때 -1 해줘야함 왜냐하면 첫날 포함이기 때문에
			menstrualCyclePeriodCalender.add(Calendar.DATE, menstrualPeriod -1);
			menstrualCycleCalendarDto.setMenstrualCycleEndDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			menstrualCycleCalendarDto.setMenstrualCycleMemo(MenstrualCycle.MENSTRUAL_CYCLE.getText());

			menstrualCyclePeriodCalender.add(Calendar.DATE, menstrualCycle);
			menstrualCyclePeriodCalender.add(Calendar.DATE, -(menstrualPeriod -1));
			menstrualCycleCalendarDto.setAfterMenstrualCycleStartDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			menstrualCyclePeriodCalender.add(Calendar.DATE, menstrualPeriod -1);
			menstrualCycleCalendarDto.setAfterMenstrualCycleEndDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			menstrualCycleCalendarDto.setAfterMenstrualCycleMemo(MenstrualCycle.MENSTRUAL_CYCLE.getText());

			menstrualCyclePeriodCalender.add(Calendar.DATE, -(menstrualPeriod -1));
			menstrualCyclePeriodCalender.add(Calendar.DATE, -menstrualCycle);
			menstrualCyclePeriodCalender.add(Calendar.DATE, -takingContraceptiveCycle);

			menstrualCycleCalendarDto.setBeforeMenstrualCycleStartDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			menstrualCyclePeriodCalender.add(Calendar.DATE, menstrualPeriod -1);
			menstrualCycleCalendarDto.setBeforeMenstrualCycleEndDt(sDate.format(menstrualCyclePeriodCalender.getTime()) + " 00:00:00.000Z");
			menstrualCycleCalendarDto.setBeforeMenstrualCycleMemo(MenstrualCycle.MENSTRUAL_CYCLE.getText());
		}

		return menstrualCycleCalendarDto;
	}

	// 배란일 구하기
	public static MenstrualCycleCalendarDto getOvulationDt(MenstrualCycleCalendarDto menstrualCycleCalendarDto) throws ParseException {
		SimpleDateFormat sDate = new SimpleDateFormat("yyyy-MM-dd");
		Calendar ovulationDateCalendar = Calendar.getInstance();
		Calendar afterOvulationDateCalendar = Calendar.getInstance();
		Date convertOvulationDt = sDate.parse(menstrualCycleCalendarDto.getMenstrualCycleStartDt());
		Date convertAfterConvertOvulationDt = sDate.parse(menstrualCycleCalendarDto.getAfterMenstrualCycleStartDt());

		ovulationDateCalendar.setTime(convertOvulationDt);
		afterOvulationDateCalendar.setTime(convertAfterConvertOvulationDt);

		ovulationDateCalendar.add(Calendar.DATE, -14);
		menstrualCycleCalendarDto.setOvulationDt(sDate.format(ovulationDateCalendar.getTime()) + " 00:00:00.000Z");
		menstrualCycleCalendarDto.setOvulationDtMemo(MenstrualCycle.OVULATION_DAY.getText());

		afterOvulationDateCalendar.add(Calendar.DATE, -14);
		menstrualCycleCalendarDto.setAfterOvulationDt(sDate.format(afterOvulationDateCalendar.getTime()) + " 00:00:00.000Z");
		menstrualCycleCalendarDto.setAfterOvulationDtMemo(MenstrualCycle.OVULATION_DAY.getText());

		return menstrualCycleCalendarDto;
	}

	// 가임기 구하기
	public static MenstrualCycleCalendarDto getFertileWindowDt(MenstrualCycleCalendarDto menstrualCycleCalendarDto) throws ParseException {
		SimpleDateFormat sDate = new SimpleDateFormat("yyyy-MM-dd");
		Calendar fertileWindowDateCalendar = Calendar.getInstance();
		Calendar afterFertileWindowDateCalendar = Calendar.getInstance();
		Date convertDate = sDate.parse(menstrualCycleCalendarDto.getOvulationDt());
		Date convertAfterDate = sDate.parse(menstrualCycleCalendarDto.getAfterOvulationDt());

		fertileWindowDateCalendar.setTime(convertDate);
		afterFertileWindowDateCalendar.setTime(convertAfterDate);

		fertileWindowDateCalendar.add(Calendar.DATE, -5);
		menstrualCycleCalendarDto.setFertileWindowStartDt(sDate.format(fertileWindowDateCalendar.getTime()) + " 00:00:00.000Z");
		fertileWindowDateCalendar.add(Calendar.DATE, 8);
		menstrualCycleCalendarDto.setFertileWindowEndDt(sDate.format(fertileWindowDateCalendar.getTime()) + " 00:00:00.000Z");
		menstrualCycleCalendarDto.setFertileWindowMemo(MenstrualCycle.FERTILE_WINDOW_DATE.getText());

		afterFertileWindowDateCalendar.add(Calendar.DATE, -5);
		menstrualCycleCalendarDto.setAfterFertileWindowStartDt(sDate.format(afterFertileWindowDateCalendar.getTime()) + " 00:00:00.000Z");
		afterFertileWindowDateCalendar.add(Calendar.DATE, 8);
		menstrualCycleCalendarDto.setAfterFertileWindowEndDt(sDate.format(afterFertileWindowDateCalendar.getTime()) + " 00:00:00.000Z");
		menstrualCycleCalendarDto.setAfterFertileWindowMemo(MenstrualCycle.FERTILE_WINDOW_DATE.getText());

		return menstrualCycleCalendarDto;
	}

	// 피임약 복용시 생리날짜 구하기
	public static MenstrualCycleCalendarDto getContraceptiveMenstrualDate(Contraceptive contraceptiveType, MenstrualCycleCalendarDto menstrualCycleCalendarDto, String takingContraceptiveDt, int menstrualCycle, int menstrualPeriod) throws ParseException {
		MenstrualCycleCalendarDto result = new MenstrualCycleCalendarDto();

		// 일단 복용 날짜가 생리 예정 날짜에 있는지 먼저 체크해야함
		// 핑크다이어리의 경우 앞뒤 한달의 데이터를 넘어가면 아에 생리주기가 없기 때문에 무조건 같은 날짜가 나옴
		// 핑크다이어리는 피임약을 먹으면 무조건 주기가 28일주기로 됨 이후 약 별로 주기가 마이너스 됨

		// 1. 약을 먹은 날짜의 년과 달을 가져옴
		// 2. 해당 년과 달이 파라미터로 가져온 생리주기의 년과 달이 겹치고 생리 날짜에 겹치면 바로 주기 생성해서 리턴
		// 3. 해당 년과 달이 파라미터로 가져온 생리주기의 년과 달이 겹치고 생리 날짜에 겹치지 않으면 효과가 없으므로 기존 생리 날짜로 리턴
		// 4. 해당 년과 달이 파라미터로 가져온 생리주기의 년과 달이 겹치지 않으면 기존에 생성된 생리주기를 토대로 해당 년과 월의 생리주기를 생성 후 이 생리주기와 겹치는지 확인
		// 5. 겹친다면 주기를

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		LocalDate paramsTakingDt = LocalDate.parse(takingContraceptiveDt, formatter);
		// LocalDate 는 00:00:00.000Z 파싱을 못함
		LocalDate beforeMenstrualStartDt = LocalDate.parse(menstrualCycleCalendarDto.getBeforeMenstrualCycleStartDt().substring(0, 10), formatter);

		if (paramsTakingDt.isBefore(beforeMenstrualStartDt)) {
			// 만약 복용 날짜가 전월의 생리 첫날보다 전일 경우 파라미터로 가져온 데이터 그대로 리턴
			return menstrualCycleCalendarDto;
		} else {
			// 복용 날짜가 전월의 생리 첫날보다 후일 경우
			Calendar beforeMenstrual = Calendar.getInstance();
			String tempResult = checkOverlapContraceptiveDt(menstrualCycleCalendarDto.getBeforeMenstrualCycleStartDt(), menstrualCycleCalendarDto.getBeforeMenstrualCycleEndDt(), takingContraceptiveDt);
			if (Objects.equals(tempResult, "NONE")) {
				tempResult = checkOverlapContraceptiveDt(menstrualCycleCalendarDto.getMenstrualCycleStartDt(), menstrualCycleCalendarDto.getMenstrualCycleEndDt(), takingContraceptiveDt);
				if (Objects.equals(tempResult, "NONE")) {
					tempResult = checkOverlapContraceptiveDt(menstrualCycleCalendarDto.getAfterMenstrualCycleStartDt(), menstrualCycleCalendarDto.getAfterMenstrualCycleEndDt(), takingContraceptiveDt);
					if (Objects.equals(tempResult, "NONE")) {
						return menstrualCycleCalendarDto;
					}
				}
			}
			MenstrualCycleCalendarDto afterResult = new MenstrualCycleCalendarDto();

			switch (contraceptiveType) {
				case CONTRACEPTIVE_A:
					// -6일
					MenstrualCycleCalendarDto menstrualCycleResultA = getMenstrualCyclePeriod(menstrualCycleCalendarDto.getBeforeMenstrualCycleStartDt(), menstrualCycle, menstrualPeriod, menstrualCycle -6);
					MenstrualCycleCalendarDto ovulationDateResultA = getOvulationDt(menstrualCycleResultA);
					MenstrualCycleCalendarDto fertileWindowDtResultA  = getFertileWindowDt(ovulationDateResultA);

//					result = getMenstrualCyclePeriod(menstrualCycleCalendarDto.getBeforeMenstrualCycleStartDt(), menstrualCycle, menstrualPeriod, menstrualCycle -6);
					result = fertileWindowDtResultA;
					break;
				case CONTRACEPTIVE_B:
					// -6일
					MenstrualCycleCalendarDto menstrualCycleResultB = getMenstrualCyclePeriod(menstrualCycleCalendarDto.getBeforeMenstrualCycleStartDt(), menstrualCycle, menstrualPeriod, menstrualCycle -6);
					MenstrualCycleCalendarDto ovulationDateResultB = getOvulationDt(menstrualCycleResultB);
					MenstrualCycleCalendarDto fertileWindowDtResultB  = getFertileWindowDt(ovulationDateResultB);

					result = fertileWindowDtResultB;
//					result = getMenstrualCyclePeriod(menstrualCycleCalendarDto.getBeforeMenstrualCycleStartDt(), menstrualCycle, menstrualPeriod, menstrualCycle -6);
					break;
				case CONTRACEPTIVE_C:
					// -3일
					MenstrualCycleCalendarDto menstrualCycleResultC = getMenstrualCyclePeriod(menstrualCycleCalendarDto.getBeforeMenstrualCycleStartDt(), menstrualCycle, menstrualPeriod, menstrualCycle -3);
					MenstrualCycleCalendarDto ovulationDateResultC = getOvulationDt(menstrualCycleResultC);
					MenstrualCycleCalendarDto fertileWindowDtResultC  = getFertileWindowDt(ovulationDateResultC);

					result = fertileWindowDtResultC;

//					result = getMenstrualCyclePeriod(menstrualCycleCalendarDto.getBeforeMenstrualCycleStartDt(), menstrualCycle, menstrualPeriod, menstrualCycle -3);
					break;
				case CONTRACEPTIVE_D:
					// -1일
					MenstrualCycleCalendarDto menstrualCycleResultD = getMenstrualCyclePeriod(menstrualCycleCalendarDto.getBeforeMenstrualCycleStartDt(), menstrualCycle, menstrualPeriod, menstrualCycle -1);
					MenstrualCycleCalendarDto ovulationDateResultD = getOvulationDt(menstrualCycleResultD);
					MenstrualCycleCalendarDto fertileWindowDtResultD  = getFertileWindowDt(ovulationDateResultD);

					result = fertileWindowDtResultD;
//					result = getMenstrualCyclePeriod(menstrualCycleCalendarDto.getBeforeMenstrualCycleStartDt(), menstrualCycle, menstrualPeriod, menstrualCycle -1);
					break;
			}
		}
		return result;
	}

	public static String checkOverlapContraceptiveDt(String startDt, String endDt, String takingContraceptiveDt) throws ParseException {
		Calendar beforeMenstrual = Calendar.getInstance();

		// 두 날짜 사이의 날짜구하기 테스트용, 첫날포함 마지막날 포함 잘 나옴
		final String DATE_PATTERN = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
		Date startDate = sdf.parse(startDt);
		Date endDate = sdf.parse(endDt);
		Date takingDt = sdf.parse(takingContraceptiveDt);
		ArrayList<String> dates = new ArrayList<String>();
		Date currentDate = startDate;

		String result = "NONE";
		while (currentDate.compareTo(endDate) <= 0) {
			dates.add(sdf.format(currentDate));
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);
			c.add(Calendar.DAY_OF_MONTH, 1);
			currentDate = c.getTime();
		}
		for (String date : dates) {
			// 여기서 date 와 takingContraceptiveDt 가 같은 것을 찾아야 함
			// 일단 약을 먹은 날짜가 생리주기에 겹치는지 확인해야하는데 이게 언제 생리때 먹었냐 이것도 중요하기 때문에 ㄷㄷ

			Date parseDate = sdf.parse(takingContraceptiveDt);
			Calendar cal = Calendar.getInstance();
			cal.setTime(parseDate);

			// 이번 생리주기안에 피임약 복용일이 있는 경우
			if (Objects.equals(date, sdf.format(takingDt))) {
				result = date;
				break;
			} else {
				// 없는 경우
				result = "NONE";
			}
		}
		return result;
	}

}

