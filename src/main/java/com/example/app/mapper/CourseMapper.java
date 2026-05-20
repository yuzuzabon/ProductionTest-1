package com.example.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.app.domain.Course;

@Mapper
public interface CourseMapper {

	//全件
		List<Course> searchAll();
	//１件
		Course searchById(Integer id);
	//登録
		void save(Course course);
}
