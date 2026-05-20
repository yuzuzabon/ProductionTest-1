package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.app.domain.Course;
import com.example.app.mapper.CourseMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
//@NoArgsConstructor
public class CourseServiceImpl implements CourseService{

		private final CourseMapper courseMapper;
		
		@Override
		public List<Course> selectCourses(){
				return courseMapper.searchAll();
		}
		@Override
		public void addCourse(Course course) {
				courseMapper.save(course);
			
		}
		@Override
		public void join(Course course) {
			// TODO 自動生成されたメソッド・スタブ
			
		}
		@Override
		public Course sellectCourseById(Integer id) {
			// TODO 自動生成されたメソッド・スタブ
			return courseMapper.searchById(id);
		}
}
