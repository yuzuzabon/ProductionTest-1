package com.example.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

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
		void insertClassRoomSchedule(ClassRoomSchedule classRoomSchedule);
}
