package com.example.app.service;

import java.util.List;

import com.example.app.domain.Course;

public interface CourseService {

	//全件
		List<Course> selectCourses();
	//１件
		Course sellectCourseById(Integer id); 
	//検索	
		
	//登録	
		void addCourse(Course course);
	//修正
		
	//申し込み	
		void join(Course course);
		 
}
