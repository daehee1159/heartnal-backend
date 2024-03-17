package com.msm.heartnal.api.service;

import com.msm.heartnal.core.dto.CoupleDiaryDto;

import java.io.IOException;
import java.util.List;

/**
 * @author 최대희
 * @since 2021-12-23
 */
public interface CoupleDiaryService {
	// Check CoupleDiary Authority
	boolean checkCoupleDiaryAuthority(Long diarySeq, Long writerMemberSeq);
	// Set CoupleDiary
	boolean setCoupleDiary(CoupleDiaryDto coupleDiaryDto) throws IOException;
	// Get CoupleDiary
	List<CoupleDiaryDto> getCoupleDiary(Long writerMemberSeq, String coupleCode);
	// Update CoupleDiary
	boolean updateCoupleDiary(CoupleDiaryDto coupleDiaryDto);
	// Delete CoupleDiary
	boolean deleteCoupleDiary(CoupleDiaryDto coupleDiaryDto);
	// Press Like
	boolean pressLike(CoupleDiaryDto coupleDiaryDto);
}
