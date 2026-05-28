package com.example.app.service;

import java.time.LocalDate;
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
		public boolean servInsertCourse(Course course) {
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
				//System.out.println("画面から届いた期間(分): " + schedule.getCoursePeriod());
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
			//Integer classRoomCode=course.getClassRoomId();//教室ID実行位置移動

		

			System.out.println("****検証用データ****");

			//登録前データと同一日、同一部屋の開始時間、終了時間取得
			for(ClassRoomSchedule s: schedules) {
				s.setClassRoomId(course.getClassRoomId());//schedulesへのclassRoomId書き込み

				}
			System.out.println("db登録前パラメータ："+schedules);

			List<LocalDate> dateList=schedules.stream()
					.map(ClassRoomSchedule::getDate)
					.filter(java.util.Objects::nonNull)
					.collect(java.util.stream.Collectors.toList());
			//schedulesのclassRoomIdとstartTimeとendTimeをもとにDB内のデータをregisteredSchedulesに格納
			List<ClassRoomSchedule>registeredSchedules=courseMapper.selectRegisteredSchedule(course.getClassRoomId(), dateList);
			System.out.println("DBから一括取得した結果: " + registeredSchedules);

			//System.out.println("テスト比較1入力された情報"+schedules);
			//System.out.println("テスト比較2データベース情報"+registeredSchedules);

			//courseMapper.selectByRoomAndDateList(course.getClassRoomId(), dateList);
					// schedules: 登録したい入力データのリスト（複数日分）
					// registeredSchedules: DBから取得した既存データのリスト（複数日分）

			boolean isOverlap=false;
			for(ClassRoomSchedule n:schedules) {
					// 入力データ1件ごとに、重複フラグをリセット
					if(servIsScheduleOverlapped(n,registeredSchedules)) {
						isOverlap=true;
						break;
						// 1つでも重複があれば、この既存データのループは抜ける
				/*	// 「同じ日付」「同じ教室」のデータ同士かチェック
					//以下判定部分をservIsScheduleOverlappedメソッドに移行
						for(ClassRoomSchedule existing : registeredSchedules) {
					if(i.getClassRoomId().equals(existing.getClassRoomId()) &&
							i.getDate().equals(existing.getDate()))
							{
					// 条件に一致するか（時間が重複するか）チェック
						if(i.getStartTime().compareTo(existing.getEndTime())<0&&
								i.getEndTime().compareTo(existing.getStartTime())>0) {
						}
					} */
					}
				}
				return isOverlap;
			}
			@Override
			@Transactional
			// チェック終了後のデータをサーバーに登録
			public void executeDbInsert(Course course, List<ClassRoomSchedule> schedules) {
					//以降登録処理
					//親テーブル（Course）の登録
					courseMapper.insertCourse(course);
					//自動採番されたコードの取得
					String generatedCode=course.getCourseId();//講座ID
					//登録前データ取得の時点で書き込み済みのため影響の検証のためコメントアウト
					//Integer classRoomCode=course.getClassRoomId();//教室ID
					CourseCapacity courseCapacity=course.getCourseCapacity();//定員

					courseCapacity.setCourseId(generatedCode);//CourseCapacityへのCourseId登録

				for(ClassRoomSchedule s : schedules) {
					s.setCourseId(generatedCode);//ClassRoomScheduleへのCourseId登録
						//↑↑へのClassRoomId登録
			//登録前データ取得の時点で書き込み済みのため影響の検証のためコメントアウト
			//	s.setClassRoomId(classRoomCode);

				}
				System.out.println("db登録後パラメータ："+schedules);//test用
				courseMapper.insertCourseCapacity(courseCapacity);
				courseMapper.insertClassRoomSchedule(schedules);
			}

//				if(!isOverlap) {
//				return "redirect:/menu";//仮の戻り場所
//				}else {
//					model.addAttribute("errorMessage", "指定された時間帯は既に他の予約と重複しています。");
//					return null;
//				}


	/*		//以降登録処理
			//親テーブル（Course）の登録
				courseMapper.insertCourse(course);
			//自動採番されたコードの取得
				String generatedCode=course.getCourseId();//講座ID
			//登録前データ取得の時点で書き込み済みのため影響の検証のためコメントアウト
				//Integer classRoomCode=course.getClassRoomId();//教室ID
				CourseCapacity courseCapacity=course.getCourseCapacity();//定員

				courseCapacity.setCourseId(generatedCode);//CourseCapacityへのCourseId登録

				for(ClassRoomSchedule s : schedules) {
					s.setCourseId(generatedCode);//ClassRoomScheduleへのCourseId登録
						//↑↑へのClassRoomId登録
			//登録前データ取得の時点で書き込み済みのため影響の検証のためコメントアウト
			//	s.setClassRoomId(classRoomCode);

				}
				System.out.println("db登録後パラメータ："+schedules);//test用
				courseMapper.insertCourseCapacity(courseCapacity);
				courseMapper.insertClassRoomSchedule(schedules);*/


			//	courseMapper.insertCourse(course);
			//	List<ClassRoomSchedule> classRoomSchedule=course.getClassRoomSchedule();
			//		course.setCourseCapacity(courseCapacity);
			//		course.setClassRoomSchedule(classRoomSchedule);

		/*		boolean isMatched=registeredSchedules.stream()
					.anyMatch(existing ->
						exising.getStartTime().equals(schedules.getStartTime())&&
						exising.getEndTime().equals(schedules.getEndTime());
							)*/
		@Override
	/* *
		 * 入力されたスケジュールが、既存のスケジュールリストと重複しているかを判定する
		 * @param newSchedule 新しく登録・修正しようとしている入力データ
		 * @param registeredSchedules DBから取得した既存データのリスト
		 * @return 重複があれば true、なければ false
		 */
		public boolean servIsScheduleOverlapped(ClassRoomSchedule newSchedule,
				List<ClassRoomSchedule>registeredSchedules) {

				for(ClassRoomSchedule existing : registeredSchedules) {
					// 「同じ日付」「同じ教室」のデータ同士かチェック
					if(newSchedule.getClassRoomId().equals(existing.getClassRoomId()) &&
							newSchedule.getDate().equals(existing.getDate()))
							{
					// 条件に一致するか（時間が重複するか）チェック
						if(newSchedule.getStartTime().compareTo(existing.getEndTime())<0&&
								newSchedule.getEndTime().compareTo(existing.getStartTime())>0) {

							return true;// 1つでも重複があれば、この既存データのループは抜ける
						}
					}
				}
				return false;// すべての既存データをチェックして、どれとも重ならなければ false を返す

				}

//			for(ClassRoomSchedule exist:existingSchedules) {
//				LocalTime existStart=exist.getStartTime();
//				LocalTime existEnd=exist.getEndTime();
//				if(newStart.isBefore(existEnd)&&newEnd.isAfter(existStart)) {
//					return true;
//				}
//					return false;
//			}
//			List<ClassRoomSchedule>existingSchedules=courseMapper.selectRegisteredSchedule(
//					newSchedule.getClassRoomId(),newSchedule.getDate());
//
//			LocalTime newStart=newSchedule.getStartTime();
//			LocalTime newEnd=newSchedule.getEndTime();

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
			//System.out.println("page数"+page+" offset"+offset+" numPerPage"+numPerPage);//test
			return courseMapper.selectCourseByPage(offset,numPerPage);
		}
		@Override
		public int servSelectTotalPages(int numPerPage) {
			// TODO 自動生成されたメソッド・スタブ
			double totalNum=(double) courseMapper.selectTotalPages();
			//System.out.println("総件数"+totalNum);//test
			return (int) Math.ceil(totalNum/ numPerPage);
		}
		@Override
		public List<ClassRoomSchedule>servSelectRegisteredSchedule(Integer classRoomId,List<LocalDate> date) {
			// TODO 自動生成されたメソッド・スタブ
			//List<ClassRoomSchedule> schedules = course.getClassRoomSchedule();
			return courseMapper.selectRegisteredSchedule(classRoomId,date);


		}


}

