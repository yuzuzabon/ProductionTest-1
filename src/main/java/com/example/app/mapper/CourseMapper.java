package com.example.app.mapper;

import java.time.LocalDate;
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
		Course selectCourseById(Integer id);
	//登録
		void insertCourse(Course course);
		void insertCourseCapacity(CourseCapacity courseCapacity);
		void insertClassRoomSchedule(@Param("classRoomSchedule")
					List<ClassRoomSchedule> classRoomSchedule);

	//ページ分割
		List<Course> selectCourseByPage(
				@Param("offset")int offset,
				@Param("limit")int limit);
		Long selectTotalPages();

	//重複チェック
		List<ClassRoomSchedule> selectByRoomAndDateList(
        @Param("classRoomId") Integer classRoomId,
        @Param("dateList") List<LocalDate> dateList);


}
