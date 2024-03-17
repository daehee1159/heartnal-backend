package com.msm.heartnal.core.mapper;

import com.msm.heartnal.core.dto.CoupleDiaryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 최대희
 * @since 2021-12-23
 */
@Mapper
public interface CoupleDiaryMapper {
	/* Set CoupleDiary */
	boolean setCoupleDiary(CoupleDiaryDto coupleDiaryDto);
	/* Get CoupleDiary */
	List<CoupleDiaryDto> getCoupleDiaryList(String coupleCode);
	CoupleDiaryDto getCoupleDiary(Long diarySeq);
	/* Update CoupleDiary */
	boolean updateCoupleDiary(CoupleDiaryDto coupleDiaryDto);
	/* Delete CoupleDiary */
	boolean deleteCoupleDiary(CoupleDiaryDto coupleDiaryDto);

	/* Restore CoupleDiary */
	boolean deleteCoupleDiaryByDisconnectCouple(String coupleCode);
	/* Restore CoupleDiary */
	boolean restoreCoupleDiary(String coupleCode);

	/* Press Like */
	boolean pressLike(CoupleDiaryDto coupleDiaryDto);
}
