package com.msm.heartnal.core.mapper;

import com.msm.heartnal.core.dto.MemberDto;
import com.msm.heartnal.core.dto.MostMatchedItemDto;
import com.msm.heartnal.core.dto.PlaySignalDto;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 최대희
 * @since 2021-07-10
 */
@Mapper
public interface PlaySignalMapper {
	// insert
	int senderPlaySignal(PlaySignalDto playSignalDto);
	// update
	int senderPlaySignalUpdate(PlaySignalDto playSignalDto);
	// update
	int recipientPlaySignal(PlaySignalDto playSignalDto);

	// select
	PlaySignalDto getPlaySignalBySignalSeq(Long playPrimarySignalSeq);

	// getPlaySignalMostMatchedItem
	MostMatchedItemDto getPlaySignalMostMatchedItem(String coupleCode);

	// delete Unresolved Signal
	boolean deleteUnResolvedPlaySignal(MemberDto memberDto);
}
