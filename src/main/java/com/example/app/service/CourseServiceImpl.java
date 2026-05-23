package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.domain.CourseCapacity;
import com.example.app.mapper.CourseMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
//@NoArgsConstructor
public class CourseServiceImpl implements CourseService{

		private final CourseMapper courseMapper;

		@Override
		public List<Course> servSelectCourseAll(){
				return courseMapper.selectCourseAll();
		}
		@Override
		@Transactional
		public void servInsertCourse(Course course) {
				courseMapper.insertCourse(course);
				String generatedCode=course.getCourseId();
				Integer classRoomCode=course.getClassRoomId();

			if (course.getCourseCapacity() == null) {
	        course.setCourseCapacity(new CourseCapacity());
	    }
	    if (course.getClassRoomSchedule() == null) {
	        course.setClassRoomSchedule(new ClassRoomSchedule());
	    }

				CourseCapacity courseCapacity=course.getCourseCapacity();
				ClassRoomSchedule classRoomSchedule=course.getClassRoomSchedule();

					course.setCourseCapacity(courseCapacity);
					course.setClassRoomSchedule(classRoomSchedule);

				courseCapacity.setCourseId(generatedCode);
				classRoomSchedule.setCourseId(generatedCode);
				classRoomSchedule.setClassRoomId(classRoomCode);

				courseMapper.insertCourseCapacity(courseCapacity);
				courseMapper.insertClassRoomSchedule(classRoomSchedule);
		}
		@Override
		public void join(Course course) {
			// TODO 自動生成されたメソッド・スタブ

		}
		@Override
		public Course servSellectCourseById(Integer id) {
			// TODO 自動生成されたメソッド・スタブ
			return courseMapper.selectCourseById(id);
		}
}
