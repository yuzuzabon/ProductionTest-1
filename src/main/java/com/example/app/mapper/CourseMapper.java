package com.example.app.mapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.domain.CourseCapacity;
import com.example.app.domain.CourseSales;

@Mapper
public interface CourseMapper {

	//全件
//		List<Course> selectCourseAll();
		List<Course> selectCourseAll(@Param("searchType") String searchType);
	//１件
		List<Course> selectCourseByCourseId(String courseId);
		//１件 public class RefundServiceImpl implements RefundService用
		List<Course> selectCourseByCourseId2(String courseId);
	//登録
		void insertCourse(Course course);
		void insertCourseCapacity(CourseCapacity courseCapacity);
		void insertClassRoomSchedule(@Param("classRoomSchedule")
					List<ClassRoomSchedule> classRoomSchedule);
	//変更 書き換え
		void updateSingleSchedule(
				@Param("id")Integer id,
				@Param("classRoomId")Integer classRoomId,
				@Param("date") LocalDate date,
				@Param("startTime")LocalTime startTime,
				@Param("endTime") LocalTime endTime);
	//変更 事前チェック
		int countOverlappedSchedule(
		    @Param("id") Integer id,
		    @Param("classRoomId") Integer classRoomId,
		    @Param("date") LocalDate date,
		    @Param("startTime") LocalTime startTime,
		    @Param("endTime") LocalTime endTime);
	//変更 変更なし上書き回避チェック
		ClassRoomSchedule selectCheckSingleScheduleByclassRoomId(
				@Param("id") Integer id);
	//ページ分割
/*
		List<Course> selectCourseByPage(
				@Param("offset")int offset,
				@Param("limit")int limit);

		Long selectTotalPages();
*/
		//
		List<Course> selectCourseByPage(
				@Param("offset")int offset,
				@Param("limit")int limit,
				@Param("searchType")String searchType
				);
		Long selectTotalPages(@Param("searchType")String searchType);


	//重複チェック
		List<ClassRoomSchedule> selectRegisteredSchedule(
        @Param("classRoomId") Integer classRoomId,
        @Param("dateList") List<LocalDate> dateList);

		//利用状況一覧
		List<ClassRoomSchedule> selectClassRoomScheduleAll();

		//日程変更course_salesリセット
		void updateCourseSalesReset(String courseId);
		void updateCourseSalesTuitionFeeReset(String courseId);

		//日程変更変更前当該講座のcourse_sales取得
		List<CourseSales>selectCourseSalesByCourseId(String courseId);


}
