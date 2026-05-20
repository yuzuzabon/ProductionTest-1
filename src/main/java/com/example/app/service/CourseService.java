package com.example.app.service;

import java.util.List;

import com.example.app.domain.Course;

public interface CourseService {

	//全件
		List<Course> searchAll();
	//１件
		
	//登録	
		void save(Course course);
}
