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
		public List<Course> searchAll(){
				return courseMapper.selectCourses();
		}
		@Override
		public void save(Course course) {
				courseMapper.addCourse(course);
			
		}
}
