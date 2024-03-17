package com.msm.heartnal.api.controller;

import com.msm.heartnal.api.service.EatSignalService;
import com.msm.heartnal.core.dto.EatSignalDto;
import com.msm.heartnal.core.jwt.enums.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author 최대희
 * @since 2021-07-10
 */
@RestController
@RequestMapping(value = "/eat-signal", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EatSignalController {
	private final EatSignalService eatSignalService;

	/**
	 * Sender 오늘 뭐먹지
	 */
	@RequestMapping( value = "/sender", method = RequestMethod.POST)
	public int senderEatSignal(@RequestBody EatSignalDto eatSignalDto) throws CommonException, Exception {
		int result = eatSignalService.senderEatSignal(eatSignalDto);

		return result;
	}

	/**
	 * Recipient 오늘 뭐먹지
	 */
	@RequestMapping( value = "/recipient", method = RequestMethod.POST)
	public int recipientEatSignal(@RequestBody EatSignalDto eatSignalDto) throws CommonException, Exception {
		int result = eatSignalService.recipientEatSignal(eatSignalDto);

		return result;
	}
}
