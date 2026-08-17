package com.example.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.app.domain.ScheduleAccountingDetail;

@Mapper
public interface MemberScheduleStatusMapper {

	/**
   * 会員IDと講座IDに紐づく会計計算用スケジュール詳細一覧を取得する
   */
	//事業者事由の払い戻し用
  List<ScheduleAccountingDetail> selectScheduleAccountingDetails(
         // @Param("memberId") Integer memberId,
          @Param("courseId") String courseId);
  //受講生事由の払い戻し用
  List<ScheduleAccountingDetail> selectCancellMemberByMemberId(
					@Param("memberId") Integer id);
  
  //事業者事由の払い戻し用= List<ScheduleAccountingDetail> selectScheduleAccountingDetails
//  List<ScheduleAccountingDetail> selectCancellScheduleBycourseId(
//					@Param("courseId") String courseId);
}
