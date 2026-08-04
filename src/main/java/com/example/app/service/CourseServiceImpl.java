package com.example.app.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.domain.CourseCapacity;
import com.example.app.domain.OccupiedRoomSchedule;
import com.example.app.domain.ScheduleUpdateRequest;
import com.example.app.mapper.CourseMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
//@NoArgsConstructor
public class CourseServiceImpl implements CourseService{

	
		private final CourseMapper courseMapper;
	//private final MemberService memberService;

		@Override
		public List<Course> servSelectCourseAll(String searchType){
				return courseMapper.selectCourseAll(searchType);
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
			
			//回数が2未満で、途中受講が「可(true)」になっている場合
			if(expectedCount != null && expectedCount < 2 
          && Boolean.TRUE.equals(course.getAllowLateEnrollment())) {
				throw new IllegalArgumentException("途中受講は1回以下の講座には設定できません");
			}


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
		public String servCheckScheduleUpdateRequest(ScheduleUpdateRequest updateRequest) {
		// 1. 終了時間の再計算（新規登録時のロジックを流用）
				LocalTime start = updateRequest.getStartTime();//開始時間
				int period = updateRequest.getCoursePeriod();//講座時間（分）
				LocalTime end = start.plusMinutes(period);//開始時間＋講座時間で終了時間を計算

		// 2. 重複チェックの実行（自分自身のIDを除外してカウント）
		    int overlapCount = courseMapper.countOverlappedSchedule(
		    		updateRequest.getId(),// 判定から除外する自分のID
		    		updateRequest.getClassRoomId(),// 調べたい教室
		    		updateRequest.getDate(),// 調べたい日付
		        start,// 新しい開始時間
		        end// 新しい終了時間
		    );

		    // 重複がある（カウントが1以上）場合は、更新せずにfalseを返して処理を中断
		    if (overlapCount > 0) {
		    	System.out.println("overlapCount="+overlapCount);
		        return "duplicate";// 重複エラーの目印を返す
		    }
// ////////test環境時は変更箇所なしチェック処理部分をコメントアウト////////
		    // 重複チェックで使っている id を活用して、DBから現在の1件を直接取得
	/*	    ClassRoomSchedule current = courseMapper.selectCheckSingleScheduleByclassRoomId(updateRequest.getId());

		    if(current != null) {
		    // 1. 過去日付への変更チェック（LocalDate.now()より前の日付は不可）
    			if (updateRequest.getDate() != null && updateRequest.getDate().isBefore(LocalDate.now())) {
        		System.out.println("****** 過去日付への変更不可");
        			return "past_date_error"; // 過去日付エラーの目印を返す
    			}
		    
		   // 2. 変更箇所なしチェック 
		    	boolean isClassRoomSame=current.getClassRoomId().equals(updateRequest.getClassRoomId());
		    	boolean isDateSame=current.getDate().equals(updateRequest.getDate());
		    	boolean isStartTimeSame =current.getStartTime().equals(updateRequest.getStartTime());
		   // 3つの項目がすべて変更前と同じなら
		    	if(isClassRoomSame && isDateSame && isStartTimeSame) {
		    		System.out.println("****** 変更箇所なし");
						return "no_change"; // 変更なしエラーの目印を返す
		    	}
		    } 
	*/	   

		    // 重複がなければMapperを呼び出してUPDATEを実行
		    // ※引数の渡し方は既存のMapperの仕様（オブジェクトに詰め直すか、個別で渡すか）に合わせて調整してください

		    courseMapper.updateSingleSchedule(updateRequest.getId(), updateRequest.getClassRoomId(), updateRequest.getDate(), start, end);

		   // System.out.println("updateRequest---"+updateRequest);
		  
		    // //////////////////////////////////////////////////////////
		
		    return "success"; // 成功の目印
		}

			@Override
			@Transactional
			// チェック終了後のデータをサーバーに登録
			public String executeDbInsert(Course course, List<ClassRoomSchedule> schedules) {
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
				return generatedCode;
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
		public List<Course> servSellectCourseByCourseId(String courseId) {
			// TODO 自動生成されたメソッド・スタブ
			return courseMapper.selectCourseByCourseId(courseId);
		}
		//ページ分割　zdrive仕様
/*		@Override
		
		public List<Course> servSelectCourseByPage
		(int page, int numPerPage) {
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
*/
		public static final int NUM_PER_PAGE=5;
	// 一覧の取得
		@Override
    public List<Course> servSelectCourseByPage(int page, String searchType) {
        int offset = (page - 1) * NUM_PER_PAGE;
        return courseMapper.selectCourseByPage(offset, NUM_PER_PAGE, searchType);
    }
		
    // 総ページ数の計算
		@Override
    public double servSelectTotalPages(String searchType) {
        Long totalCount = courseMapper.selectTotalPages(searchType);
        if (totalCount == null || totalCount == 0) {
            return 0;
        }
        // 総件数 ÷ 1ページ件数 を切り上げ計算
        return Math.ceil((double) totalCount / NUM_PER_PAGE);
    }
		
		@Override
		public List<ClassRoomSchedule>servSelectRegisteredSchedule(Integer classRoomId,List<LocalDate> date) {
			// TODO 自動生成されたメソッド・スタブ
			//List<ClassRoomSchedule> schedules = course.getClassRoomSchedule();
			return courseMapper.selectRegisteredSchedule(classRoomId,date);


		}

		@Override
		public OccupiedRoomSchedule servSelectClassRoomScheduleAll(LocalDate baseDate) {
			// TODO 自動生成されたメソッド・スタブ LocalDate baseDateはcontrollerから新しい基準日付受け取り用
			List<LocalDate>dateList=servGenerateDateList(baseDate);
			List<LocalTime>timeList=servGenerateTimeList();

			//System.out.println("------1週間分の日付------"+dateList);
			//System.out.println("------1日分の時刻------"+timeList);

			List<ClassRoomSchedule> roomSchedules=courseMapper.selectClassRoomScheduleAll();
			Map<String,Set<String>>scheduleMap=new TreeMap<>();
			//占有情報作成用の箱作成
			for(ClassRoomSchedule rs:roomSchedules) {
				LocalTime current=rs.getStartTime();//開始時間
				String classRoom=rs.getClassRoom().getClassRoom();//部屋名
					while(current.isBefore(rs.getEndTime())) {//スケジュールがあるところに
						String key=rs.getDate()+"_"+current;		//終了時間まで30分単位に日付_時間のデータを作る
																										//60分->2コマ 90分->3コマ 120分->4コマ
						scheduleMap.computeIfAbsent(classRoom, k -> new HashSet<>())
						.add(key);
						current=current.plusMinutes(30);

					}
				}
		// ガワ（dateList × timeList）と使用済みデータを組み合わせたマトリクスを作成
			Map<String,Map<String,Boolean>>occupiedMap=new LinkedHashMap<String, Map<String,Boolean>>();
		// 部屋ごとにマトリクスを埋めていく
			for(String cr : scheduleMap.keySet()) {
				Map<String,Boolean>occupied=new LinkedHashMap<>();
				for(LocalDate date:dateList) {
					for(LocalTime time:timeList) {
						String key=date+"_"+time;
		// 使用済みマップにキーが存在すれば true (予約あり), なければ false (空き)
						Boolean isReserved=scheduleMap.get(cr).contains(key);
						occupied.put(key, isReserved);
					}
				}
				occupiedMap.put(cr, occupied);
			}
			System.out.println("--利用状況--service側取得--"+occupiedMap);


			return new OccupiedRoomSchedule(dateList,timeList,occupiedMap);
		}
			//Map<LocalDate,List<LocalTime>>scheduleTemp=new HashMap<>();
			//mapよりも縦軸横軸別のlistを渡した方がthymeleaf上の処理が簡単になる
		private List<LocalDate>servGenerateDateList(LocalDate baseDate){
			LocalDate sunday=baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
		//LocalDate sunday=LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

		//LocalDate sunday=day.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
		//today.minusDays(today.getDayOfWeek().getValue() % 7	);
			List<LocalDate>dateList=new ArrayList<>();

				for(int i=0;i<7;i++) {
			//scheduleTemp.put(sunday.plusDays(i), timeList);
					dateList.add(sunday.plusDays(i));
		}
				return dateList;
		}

		private	List<LocalTime>servGenerateTimeList(){
			List<LocalTime>timeList=new ArrayList<>();
			LocalTime start=LocalTime.of(10, 0);
			LocalTime end=LocalTime.of(16, 30);

				while (!start.isAfter(end)) {
					timeList.add(start);
					start=start.plusMinutes(30);
			}
					return timeList;
		}


}

