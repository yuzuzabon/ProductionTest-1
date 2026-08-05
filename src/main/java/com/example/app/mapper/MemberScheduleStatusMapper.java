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
  List<ScheduleAccountingDetail> selectScheduleAccountingDetails(
         // @Param("memberId") Integer memberId,
          @Param("courseId") String courseId);
}
