package com.example.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.app.domain.CourseSalesRefund;
@Mapper
public interface RefundMapper {
		
		// 講座中止 class_room_scheduleのschedule_statusカラムへの書き込み
		void updateCrsStatusForCancellation(
				@Param("targetIds") List<Integer> targetIds,
				@Param("status") String status
				);
		// 講座中止 course_historyカラムへの書き込み
		void updateCourseHistoryForRefund(
				@Param("memberId") Integer memberId,
				@Param("courseId") String courseId,
				@Param("refundtuitionFee") int refundtuitionFee,
				@Param("refundmaterialFee") int refundmaterialFee
				
				);
		// 講座中止 course_salesカラムへの書き込み
		void updateCourseSalesForRefund(
				@Param("list") List<CourseSalesRefund>dtoList
				);
		
}
