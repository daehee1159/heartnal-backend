package com.msm.heartnal.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 최대희
 * @since 2021-09-14
 */
@Getter
@Setter
public class SendAndReceiveSignalCount {
	private int eatSendSignalCount;
	private int eatReceiveSignalCount;
	private int playSendSignalCount;
	private int playReceiveSignalCount;

	private int sendSignalCount;
	private int receiveSignalCount;
}
