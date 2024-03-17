package com.msm.heartnal.core.dto.calendar;

import com.msm.heartnal.core.enums.Contraceptive;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2022-08-03
 */
@Getter
@Setter
public class MenstrualCycleDto {
	private Long menstrualCycleSeq;
	private Long memberSeq;
	private Long coupleMemberSeq;
	private String coupleCode;

	private String lastMenstrualStartDt;
	private Integer menstrualCycle;
	private Integer menstrualPeriod;
	private String contraceptiveYN;
	private String takingContraceptiveDt;
	private Contraceptive contraceptive;

	private String modDt;
	private String regDt;
}
