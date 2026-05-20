package com.example.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.app.domain.Course;

@Mapper
public interface CourseMapper {

	//全件
		List<Course> selectCourses();
	//１件
		
	//登録
		void addCourse(Course course);
}
