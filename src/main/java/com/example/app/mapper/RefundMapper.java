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
				@Param("refundTuitionFee") int refundTuitionFee,
				@Param("refundMaterialFee") int refundMaterialFee

				);
		// 講座中止 course_salesカラムへの書き込み
		void updateCourseSalesForRefund(
				@Param("list") List<CourseSalesRefund>dtoList
				);
		// 講座休講 class_room_scheduleのschedule_statusカラムへの書き込み
		void updateCrsStatusForCancelledSession(
				@Param("targetId") Integer targetId,
				@Param("status") String status
				);

		void updateCourseSalesMaterialFee(
		    @Param("courseId") String courseId,
		    @Param("currentStartMonth") String currentStartMonth,
		    @Param("newStartMonth") String newStartMonth,
		    @Param("pmFee") Integer pmFee
				);
		//受講生事由払い戻し
		void updateCourseHistoryForMemberRefundRefund(
				@Param("memberId") Integer memberId,
				@Param("courseId") String targetCourseId,
				@Param("refundTuitionFee") int refundTuitionFee,
				@Param("refundMaterialFee") int refundMaterialFee);
		
		void updateCourseSalesForMemberRefund(
				@Param("list") List<CourseSalesRefund> refundList);
		
		void updateCourseCapacityForMemberRefund(
				@Param("targetId")String targetCourseId);

}
