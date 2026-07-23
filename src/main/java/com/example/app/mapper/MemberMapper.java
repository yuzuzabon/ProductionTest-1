package com.example.app.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.app.domain.Course;
import com.example.app.domain.CourseCapacity;
import com.example.app.domain.CourseHistory;
import com.example.app.domain.CourseSales;
import com.example.app.domain.Member;
import com.example.app.domain.MonthlyCount;

@Mapper
public interface MemberMapper {
	//全件
	List<Member>selectMemberAll();
	//idまたは名前検索	
	List<Member>selectMemberByWord(
				@Param("id")Integer id,
				@Param("name")String name);
	//id
	public Member selectMemberById(
				@Param("id") Integer id);
	//受講申し込み
	Optional<CourseCapacity>selectByCourseIdForUpdate(
				@Param("courseId") String courseId);
	//申込後の申込者数加算
	int updateApplyedCount(
				@Param("courseId")String courseId);
			//	numberOfApplicant=numberOfApplicant+1をSQL側で処理
			//@Param("courseId")String courseId, int numberOfApplicant);
	//講座申し込み時の受講料振り分けのためのスケジュールと受講料獲得用
	List<MonthlyCount> selectMonthlyCount(
				@Param("courseId")String courseId);
	public Course selectCourseFee(
				@Param("courseId")String courseId);
	//講座申し込み後の受講料等書き込み
	void upsertCourseSales(CourseSales courseSales);
				//@Param("courseId")String courseId);
	//日程変更course_sales変更
	//void updateCourseSales(CourseSales courseSales);
	//受講履歴への書き込み
	void insertCourseHistory(CourseHistory courseHistory);
	//重複受講チェック
	int countOverlappedCourse(CourseHistory history);
	//講座申し込み履歴取得
	List<CourseHistory>sellectCourseHistoryById(
				@Param("memberId") Integer memberId);
}
