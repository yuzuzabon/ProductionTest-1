package com.example.app.service;

import java.time.LocalTime;
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
			//チェック処理
			//null対策
			if (course.getCourseCapacity() == null) {
					course.setCourseCapacity(new CourseCapacity());
			}
			if (course.getClassRoomSchedule() == null) {
					course.setClassRoomSchedule(new ArrayList<>());
			}
			// スケジュール時間の補完・計算処理（Controllerから移動）
			List<ClassRoomSchedule> schedules = course.getClassRoomSchedule();
			// 共通情報を変数に退避
			LocalTime baseStartTime = null;
			if(!schedules.isEmpty()) {
				// 1件目（インデックス0）に画面全体の共通情報（開始時間、教室など）が入っているため、これを基準にする
					ClassRoomSchedule baseSchedule = schedules.get(0);
					baseStartTime=baseSchedule.getStartTime();
			}
			for(ClassRoomSchedule schedule :schedules) {
				System.out.println("画面から届いた期間(分): " + schedule.getCoursePeriod());
				if(schedule.getStartTime()==null) {
						schedule.setStartTime(baseStartTime);

				}
				if (schedule.getStartTime() != null && schedule.getCoursePeriod() != null) {
					LocalTime start = schedule.getStartTime();//開始時間
					int period = schedule.getCoursePeriod();//講座時間（分）
					LocalTime end = start.plusMinutes(period);//開始時間＋講座時間で終了時間を計算

					// 計算した終了時間をセットする
					schedule.setEndTime(end);

				}
			}


			//データ件数チェック
			Integer expectedCount=course.getCourseTerm();//講座回数
			Integer actualCount=schedules.size();

			if(!expectedCount.equals(actualCount)) {
				throw new IllegalArgumentException("設定された講座回数（" + expectedCount + "回）と、選択された開催日の日数（" + actualCount + "日）が一致しません。");
			}
			Integer classRoomCode=course.getClassRoomId();//教室ID実行位置移動
			
			//testここから
			for(ClassRoomSchedule s: schedules) {
				System.out.println("testデータ");
				System.out.print(s.getClassRoomId()+" ");
				System.out.print(s.getDate()+" ");
				}
			//テストここまで
			
			
			//以降登録処理
			//親テーブル（Course）の登録
				courseMapper.insertCourse(course);
			//自動採番されたコードの取得
				String generatedCode=course.getCourseId();//講座ID
				//Integer classRoomCode=course.getClassRoomId();//教室ID
				CourseCapacity courseCapacity=course.getCourseCapacity();//定員

				courseCapacity.setCourseId(generatedCode);//CourseCapacityへのCourseId登録

				for(ClassRoomSchedule s : schedules) {
					s.setCourseId(generatedCode);//ClassRoomScheduleへのCourseId登録
					s.setClassRoomId(classRoomCode);//↑↑へのClassRoomId登録

				}
				System.out.println(schedules);//test用
				courseMapper.insertCourseCapacity(courseCapacity);
				courseMapper.insertClassRoomSchedule(schedules);
		}

			//	courseMapper.insertCourse(course);
			//	List<ClassRoomSchedule> classRoomSchedule=course.getClassRoomSchedule();
			//		course.setCourseCapacity(courseCapacity);
			//		course.setClassRoomSchedule(classRoomSchedule);

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
			return courseMapper.selectCourseByPage(offset,numPerPage);
		}
		@Override
		public int servSelectTotalPages(int numPerPage) {
			// TODO 自動生成されたメソッド・スタブ
			double totalNum=(double) courseMapper.selectTotalPages();
			return (int) Math.ceil(totalNum/ numPerPage);
		}
		@Override
		public void servselectscheduleAll(Course course) {
			// TODO 自動生成されたメソッド・スタブ
			List<ClassRoomSchedule> schedules = course.getClassRoomSchedule();
			
			
			
		}
		

}

