package com.example.app.service;

import java.time.LocalDate;
import java.util.List;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;

public interface CourseService {

	//全件
		List<Course> servSelectCourseAll();
	//１件
		Course servSellectCourseById(Integer id);
	//検索

	//登録
		void servInsertCourse(Course course);

	//修正

	//申し込み
		void join(Course course);
	//ページ分割
		List<Course> servSelectCourseByPage(int page, int numPerPage);
		int servSelectTotalPages(int numPerPage);
	//登録重複チェック	用(登録前のclassRoomIdとdateを取り出す)
		public List<ClassRoomSchedule>servSelectRegisteredSchedule(Integer classRoomId,List<LocalDate> date);
	//登録重複チェック用（取り出したデータと入力データを比較する）	
		public boolean servIsScheduleOverlapped(ClassRoomSchedule newSchedule,
				List<ClassRoomSchedule>registeredSchedules); 
}
