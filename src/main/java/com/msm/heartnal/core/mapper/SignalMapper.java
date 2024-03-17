package com.msm.heartnal.core.mapper;

import com.msm.heartnal.core.dao.MemberDao;
import com.msm.heartnal.core.dto.MostMatchedSignalItemDto;
import com.msm.heartnal.core.dto.RecentSignalDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 최대희
 * @since 2021-09-09
 */
@Mapper
public interface SignalMapper {
	List<RecentSignalDto> getEatRecentSignal(MemberDao memberDao);
	List<RecentSignalDto> getPlayRecentSignal(MemberDao memberDao);

	List<RecentSignalDto> getAllEatSignalList(MemberDao memberDao);
	List<RecentSignalDto> getAllPlaySignalList(MemberDao memberDao);

	MostMatchedSignalItemDto getMostMatchedEatSignalItem(MostMatchedSignalItemDto mostMatchedSignalItemDto);
	MostMatchedSignalItemDto getMostMatchedPlaySignalItem(MostMatchedSignalItemDto mostMatchedSignalItemDto);

	boolean deleteEatSignal(String coupleCode);
	boolean deletePlaySignal(String coupleCode);

	boolean restoreEatSignal(String coupleCode);
	boolean restorePlaySignal(String coupleCode);
}
