package com.msm.heartnal.core.mapper;

import com.msm.heartnal.core.dto.EatSignalDto;
import com.msm.heartnal.core.dto.MemberDto;
import com.msm.heartnal.core.dto.MostMatchedItemDto;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 최대희
 * @since 2021-07-10
 */
@Mapper
public interface EatSignalMapper {
	// insert
	int senderEatSignal(EatSignalDto eatSignalDto);
	// update
	int senderEatSignalUpdate(EatSignalDto eatSignalDto);
	// update
	int recipientEatSignal(EatSignalDto eatSignalDto);

	// select
	EatSignalDto getEatSignalBySignalSeq(Long eatPrimarySignalSeq);

	// getEatSignalMostMatchedItem
	MostMatchedItemDto getEatSignalMostMatchedItem(String coupleCode);

	// delete Unresolved Signal
	boolean deleteUnResolvedEatSignal(MemberDto memberDto);
}
