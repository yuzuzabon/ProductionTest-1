package com.example.app.service;

import java.util.List;

import com.example.app.domain.Course;
import com.example.app.domain.CourseHistory;
import com.example.app.domain.Member;
import com.example.app.domain.MonthlyCount;
import com.example.app.domain.RemainingCourseData;
import com.example.app.domain.ScheduleAccountingDetail;

public interface MemberService {

	//全件
		List<Member>servSelectMemberAll();
	//idまたは名前検索	
		List<Member>servSelectMemberByWord(Integer id,String name);
	//id
		public Member servSelectMemberById(Integer id);
	//講座申し込み　
		public boolean servApplyCourse(Integer id,String courseId);
	//途中からの講座申し込み
		public boolean servRemainingApplyCourse(Integer id,String courseId);
	//講座申し込み時の受講料振り分け用
		public List<MonthlyCount> selectMonthlyCount(String courseId);
		public Course selectCourseFee(String courseId);
	//	
		public List<CourseHistory>servSellectCourseHistoryById(Integer id);
	//講座申し込み時の当該講座受講履歴有無と払い戻し判定用	
		public CourseHistory servSellectCourseHistoryWithcourseId(Integer id,String courseId);
	//講座日程変更時の受講料振り分け用
		public void servScheduleChangeCourse(String courseId);
	//講座日程変更時の変更前データ取得
	//	public List<CourseSales>selectCourseSalesByCourseId(String courseId);
	//途中受講用データ取得
		public RemainingCourseData servSelectRemainingCourseData(String courseId);
	//初回からの受講or途中受講判定
		public boolean servisFullCourseEnrollment(String courseId);
	//途中受講が可否判定
		public boolean servisValidLateEnrollment(String courseId);
	//受講履歴詳細情報取得
		public List<Course>servSellectCourseByMemberId(Integer id);
	//受講生事由の払い戻し用
		public List<ScheduleAccountingDetail>
			servSelectCancellMemberById(Integer id);
	//	
		public void recordCourseSalesLog
		(String courseId,String reasonType,String targetMonth,
				Integer beforeTuition,Integer beforeMaterial,
				Integer afterTuition,Integer afterMaterial,
				Integer refundMonthlyTuitionFee,Integer refundMaterialFee);
		
		
}
