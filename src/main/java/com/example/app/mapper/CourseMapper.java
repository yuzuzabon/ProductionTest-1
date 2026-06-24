package com.example.app.mapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.domain.CourseCapacity;

@Mapper
public interface CourseMapper {

	//全件
		List<Course> selectCourseAll();
	//１件
		List<Course> selectCourseByCourseId(String courseId);
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
		ClassRoomSchedule selectCheckSingleScheduleById(
				@Param("id") Integer id);

	//ページ分割
		List<Course> selectCourseByPage(
				@Param("offset")int offset,
				@Param("limit")int limit);
		Long selectTotalPages();

	//重複チェック
		List<ClassRoomSchedule> selectRegisteredSchedule(
        @Param("classRoomId") Integer classRoomId,
        @Param("dateList") List<LocalDate> dateList);

		//利用状況一覧
		List<ClassRoomSchedule> selectClassRoomScheduleAll();



}
