package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dto.PlaySignalDto;
import com.msm.heartnal.core.jwt.enums.CommonException;

/**
 * @author 최대희
 * @since 2021-07-14
 */
public interface PlaySignalService {
	// Sender
	int senderPlaySignal(PlaySignalDto playSignalDto) throws CommonException, Exception;
	// Recipient
	int recipientPlaySignal(PlaySignalDto playSignalDto) throws CommonException, Exception;
}
