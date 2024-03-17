package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dto.EatSignalDto;
import com.msm.heartnal.core.jwt.enums.CommonException;

/**
 * @author 최대희
 * @since 2021-07-10
 */
public interface EatSignalService {
	// Sender
	int senderEatSignal(EatSignalDto eatSignalDto) throws CommonException, Exception;
	// Recipient
	int recipientEatSignal(EatSignalDto eatSignalDto) throws CommonException, Exception;
}
