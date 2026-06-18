package com.example.app.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.app.domain.Course;
import com.example.app.domain.CourseCapacity;
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
				@Param("courseId")String courseId, int numberOfApplicant);
	//講座申し込み時の受講料振り分けのためのスケジュールと受講料獲得用
	List<MonthlyCount> selectMonthlyCount(
				@Param("courseId")String courseId);
	public Course selectCourseFee(
				@Param("courseId")String courseId);
	//講座申し込み後の受講料等書き込み
	void upsertCourseSales(CourseSales courseSales);
				//@Param("courseId")String courseId);
	
}
