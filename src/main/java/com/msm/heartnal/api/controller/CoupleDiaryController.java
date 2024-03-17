package com.msm.heartnal.api.controller;

import com.msm.heartnal.api.service.CoupleDiaryService;
import com.msm.heartnal.core.dto.CoupleDiaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @author 최대희
 * @since 2021-12-23
 * 이모지 등 특수문자를 DB에 Insert 하기 위해서는 DB Schema 를 바꿔야해서 따로 DB 생성함
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/diary", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CoupleDiaryController {
	private final CoupleDiaryService coupleDiaryService;

	/**
	 * Check CoupleDiary Authority
	 */
	@RequestMapping(value = "/check/authority/{diarySeq}/{writerMemberSeq}", method = RequestMethod.GET)
	public boolean checkCoupleDiaryAuthority(@PathVariable Long diarySeq, @PathVariable Long writerMemberSeq) {
		return coupleDiaryService.checkCoupleDiaryAuthority(diarySeq, writerMemberSeq);
	}

	/**
	 * Set CoupleDiary
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public boolean setCoupleDiary(@RequestBody CoupleDiaryDto coupleDiaryDto) throws IOException {
		return coupleDiaryService.setCoupleDiary(coupleDiaryDto);
	}

	/**
	 * Get CoupleDiary
	 */
	@RequestMapping(value = "/{writerMemberSeq}/{coupleCode}", method = RequestMethod.GET)
	public List<CoupleDiaryDto> getCoupleDiary(@PathVariable Long writerMemberSeq, @PathVariable String coupleCode) {
		return coupleDiaryService.getCoupleDiary(writerMemberSeq, coupleCode);
	}

	/**
	 * Update CoupleDiary
	 */
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public boolean updateCoupleDiary(@RequestBody CoupleDiaryDto coupleDiaryDto) {
		return coupleDiaryService.updateCoupleDiary(coupleDiaryDto);
	}

	/**
	 * Delete CoupleDiary
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public boolean deleteCoupleDiary(@RequestBody CoupleDiaryDto coupleDiaryDto) {
		return coupleDiaryService.deleteCoupleDiary(coupleDiaryDto);
	}

	/**
	 * Press Like
	 */
	@RequestMapping(value = "/like", method = RequestMethod.POST)
	public boolean pressLike(@RequestBody CoupleDiaryDto coupleDiaryDto) {
		return coupleDiaryService.pressLike(coupleDiaryDto);
	}
}
