package com.example.app.service;

import java.util.ArrayList;
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
@Transactional(rollbackFor = Exception.class) 
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
			//null対策
			if (course.getCourseCapacity() == null) {
					course.setCourseCapacity(new CourseCapacity());
			}
			if (course.getClassRoomSchedule() == null) {
					course.setClassRoomSchedule(new ArrayList<>());
			}
			//データ件数チェック
			Integer expectedCount=course.getNumberOfDays();//講座回数
			List<ClassRoomSchedule> schedules = course.getClassRoomSchedule();
			Integer actualCount=schedules.size();

			if(!expectedCount.equals(actualCount)) {
				throw new IllegalArgumentException("設定された講座回数（" + expectedCount + "回）と、選択された開催日の日数（" + actualCount + "日）が一致しません。");
			}
			//親テーブル（Course）の登録
				courseMapper.insertCourse(course);
			//自動採番されたコードの取得
				String generatedCode=course.getCourseId();//講座ID
				Integer classRoomCode=course.getClassRoomId();//教室ID
				CourseCapacity courseCapacity=course.getCourseCapacity();//定員

				courseCapacity.setCourseId(generatedCode);//CourseCapacityへのCourseId登録

				for(ClassRoomSchedule s : schedules) {
					s.setCourseId(generatedCode);//ClassRoomScheduleへのCourseId登録
					s.setClassRoomId(classRoomCode);//↑↑へのClassRoomId登録

				}

				courseMapper.insertCourseCapacity(courseCapacity);
				courseMapper.insertClassRoomSchedule(schedules);

			//	courseMapper.insertCourse(course);
			//	List<ClassRoomSchedule> classRoomSchedule=course.getClassRoomSchedule();
			//		course.setCourseCapacity(courseCapacity);
			//		course.setClassRoomSchedule(classRoomSchedule);

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
		//ページ分割
		@Override
		public List<Course> servSelectCourseByPage(int page, int numPerPage) {
			// TODO 自動生成されたメソッド・スタブ
			int offset=numPerPage*(page-1);
			return courseMapper.selectLimited(offset,numPerPage);
		}
		@Override
		public int servTotalPages(int numPerPage) {
			// TODO 自動生成されたメソッド・スタブ
			double totalNum=(double) courseMapper.count();
			return (int) Math.ceil(totalNum/ numPerPage);
		}
}
