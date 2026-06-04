package com.example.app.service;

import java.time.LocalDate;
import java.util.List;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.domain.OccupiedRoomSchedule;

public interface CourseService {

	//全件
		List<Course> servSelectCourseAll();
	//１件
		List<Course> servSellectCourseById(String id);
	//検索


	//修正

	//スケジュール利用状況一覧
	//public List<ClassRoomSchedule> servSelectClassRoomScheduleAll();
	//public Map<String,Set<String>> servSelectClassRoomScheduleAll();
		public OccupiedRoomSchedule servSelectClassRoomScheduleAll(LocalDate baseDate);
	//スケジュールマトリクスのガワ作成 
		
		
	//申し込み
		void join(Course course);
	//ページ分割
		List<Course> servSelectCourseByPage(int page, int numPerPage);
		int servSelectTotalPages(int numPerPage);
		
	//登録 重複チェック用
		public boolean servInsertCourse(Course course);
	//登録 重複チェック	用(登録前のclassRoomIdとdateを取り出す)
		public List<ClassRoomSchedule>servSelectRegisteredSchedule(Integer classRoomId,List<LocalDate> date);
	//登録 重複チェック用（取り出したデータと入力データを比較する）
		public boolean servIsScheduleOverlapped(ClassRoomSchedule newSchedule,
				List<ClassRoomSchedule>registeredSchedules);
	//登録 チェック後のデータをサーバに登録する 戻り値にcourseIDをreturnする
		public String executeDbInsert(Course course, List<ClassRoomSchedule> schedules) ;
}
