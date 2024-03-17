package com.msm.heartnal.api.controller;

import com.msm.heartnal.api.service.PlaySignalService;
import com.msm.heartnal.core.dto.PlaySignalDto;
import com.msm.heartnal.core.jwt.enums.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 최대희
 * @since 2021-07-10
 */
@RestController
@RequiredArgsConstructor
public class PlaySignalController {
	private final PlaySignalService playSignalService;

	/**
	 * Sender 오늘 뭐하지
	 */
	@RequestMapping( value = "/play-signal/sender", method = RequestMethod.POST)
	public int senderEatSignal(@RequestBody PlaySignalDto playSignalDto) throws CommonException, Exception {
		int result = playSignalService.senderPlaySignal(playSignalDto);

		return result;
	}

	/**
	 * Recipient 오늘 뭐하지
	 */
	@RequestMapping( value = "/play-signal/recipient", method = RequestMethod.POST)
	public int recipientEatSignal(@RequestBody PlaySignalDto playSignalDto) throws CommonException, Exception {
		int result = playSignalService.recipientPlaySignal(playSignalDto);

		return result;
	}
}
