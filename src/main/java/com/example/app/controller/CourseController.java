package com.example.app.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.domain.OccupiedRoomSchedule;
import com.example.app.domain.RefundSummary;
import com.example.app.domain.RemainingCourseData;
import com.example.app.domain.ScheduleUpdateRequest;
import com.example.app.service.ClassRoomService;
import com.example.app.service.CourseService;
import com.example.app.service.MemberService;
import com.example.app.service.RefundService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor

public class CourseController {

		private final CourseService courseService;
		private final MemberService memberService;
		private final ClassRoomService classRoomService;
		private final RefundService refundService;
		//private final int NUM_PER_PAGE=5;//1ページに表示される件数
		//private final ClassRoomScheduleService classRoomScheduleService;

		public String messageDuplicate="選択した教室は使用されています";
		public String messageRegistrationComplete="講座を登録しました";
		public String messageRegistrationChange="スケジュールを変更しました";


		@ModelAttribute("classRoomList")
		public List<ClassRoomService> populateClassRooms() {
		return classRoomService.servSelectRoomAll();

		}

		@GetMapping()
			public String preview() {
			return "menu";
		}

		@GetMapping("/menu")
			public String showMenu() {
			return "menu";
		}

		@GetMapping("/show")
		//講座全件取得
//		public String contSelectCourseAll(Model model) {
//			model.addAttribute("courses",courseService.servSelectCourseAll());
		public String contSelectCourseAll(
				@RequestParam(name = "searchType", defaultValue = "all") String searchType,
				@RequestParam(name="page",defaultValue = "1")Integer page,
				Model model) {

			List<Course>courses=courseService.servSelectCourseByPage(page,searchType);
			double totalPages=courseService.servSelectTotalPages(searchType);

			model.addAttribute("courses",courses);
			model.addAttribute("searchType", searchType);
			model.addAttribute("page", page);
			model.addAttribute("totalPages", (int)totalPages);

			return "courseList";
		}
/*		@GetMapping("/show") zdrive仕様のページネーション機能
	//講座全件取得
		public String contSelectCourseAll(Model model) {
			model.addAttribute("courses",courseService.servSelectCourseAll());

		//ページ分割対応
			public String contSelectCourseByPage(
					@RequestParam(name="page",defaultValue = "1")Integer page,
					Model model) {
				model.addAttribute("courses",
						courseService.servSelectCourseByPage(page, NUM_PER_PAGE));
				model.addAttribute("page",page);
				model.addAttribute("totalPages",
						courseService.servSelectTotalPages(NUM_PER_PAGE));

			return "courseList";
		}
*/

		@GetMapping("/show/{courseId}")
			public String contSelectCourseByCourseId(
					@PathVariable String courseId,
					@RequestParam(name = "searchType", defaultValue = "all") String searchType,
					@RequestParam(defaultValue = "1") Integer page,
					Model model	) {

					List<Course> course=courseService.servSellectCourseByCourseId(courseId);
					model.addAttribute("course",course);
					model.addAttribute("searchType", searchType);
					model.addAttribute("page", page);
				//System.out.println("******"+course);
					return "courseInfo";
		}

		@PostMapping("/updateSchedule")
		//@ResponseBody
		public String contCheckScheduleUpdateRequest(//(@ModelAttribute("classRoomSchedule")
				@Validated ScheduleUpdateRequest scheduleUpdateRequest,
				Errors errors,
				RedirectAttributes rd,
				@RequestParam Integer page,
				@RequestParam(name = "searchType", defaultValue = "lateEnrollment") String searchType,
				Model model) {
				rd.addAttribute("page", page);
				rd.addAttribute("searchType", searchType);

			String courseId=scheduleUpdateRequest.getCourseId();

			if (errors.hasErrors()) {
        rd.addFlashAttribute("errorMessage", "入力内容に不備があります。正しい値を入力してください");
        return "redirect:/show/" + courseId;
    }

			String result = courseService.servCheckScheduleUpdateRequest(scheduleUpdateRequest);

	    if ("success".equals(result)) {
	        rd.addFlashAttribute("statusMessage", messageRegistrationChange);
	    } else if("duplicate".equals(result)){
	        rd.addFlashAttribute("errorMessage", messageDuplicate);
	    } else if("no_change".equals(result)) {
	    // 💡 変更なしの場合のメッセージを設定
	    		rd.addFlashAttribute("errorMessage", "変更箇所がありません。");
	    } else if("past_date_error".equals(result)) {
		    // 過去日付への変更場合のメッセージを設定
    		rd.addFlashAttribute("errorMessage", "過去日付への変更はできません。");
    }
	    // course_salesの変更をログに記録

	    // course_salesのリセット

	    // course_salesの更新
	    	memberService.servScheduleChangeCourse(courseId);

				return "redirect:/show/" + courseId;
	}

		@GetMapping("/add")
		public String showInsertCourse(Model model) {
				Course course=new Course();

				List<ClassRoomSchedule> list=new ArrayList<>();
				ClassRoomSchedule initialSchedule = new ClassRoomSchedule();

		    // 初期値「60」を設定します
		    initialSchedule.setCoursePeriod(60);

				list.add(initialSchedule);
				course.setClassRoomSchedule(list);
				model.addAttribute("course",course);
				//model.addAttribute("room", classRoomService.servSelectRoomAll());
				return "insertCourse";
		}
		@PostMapping("/add")
		public String contInsertCourse(@ModelAttribute("course")
				@Validated Course course,
								Errors errors,
								Model model,
								RedirectAttributes rd) {
			if(errors.hasErrors()) {
				//model.addAttribute("room", classRoomService.servSelectRoomAll());
					return "insertCourse";
			}

			try {
				boolean isOverlap=courseService.servInsertCourse(course);//nullチェック 重複チェック

			if (!isOverlap) {
				List<ClassRoomSchedule> schedules = course.getClassRoomSchedule();
				String courseId = courseService.executeDbInsert(course, schedules);
				//courseService.executeDbInsert(course, schedules);
				//model.addAttribute("status","講座を登録しました");
				rd.addFlashAttribute("statusMessage", messageRegistrationComplete);
				return "redirect:/show/"+courseId;
			}else{
				model.addAttribute("errorMessage", messageDuplicate);
		    return "insertCourse"; // 入力画面へ

			}
			}catch(IllegalArgumentException e) {
				model.addAttribute("errorMessage",e.getMessage());
				return "insertCourse";
			}
		}
//		/////////////////test////////////////////////
		@GetMapping("/test")
		public String showAddForm(Model model) {

		    Course course = new Course();

		    List<ClassRoomSchedule> schedules = new ArrayList<>();

		    ClassRoomSchedule s1 = new ClassRoomSchedule();
		    //s1.setCourseId("2026050007");
		    s1.setClassRoomId(1);
		    s1.setDate(LocalDate.of(2026, 5, 26));
		    s1.setStartTime(LocalTime.of(11, 30));
		    s1.setEndTime(LocalTime.of(12, 00));
		    s1.setCoursePeriod(30);

		    ClassRoomSchedule s2 = new ClassRoomSchedule();
		    //s2.setCourseId("2026050007");
		    s2.setClassRoomId(1);
		    s2.setDate(LocalDate.of(2026, 5, 27));
		    s2.setStartTime(LocalTime.of(11, 30));
		    s2.setEndTime(LocalTime.of(12, 00));
		    s2.setCoursePeriod(30);

		    ClassRoomSchedule s3 = new ClassRoomSchedule();
		   // s3.setCourseId("2026050007");
		    s3.setClassRoomId(1);
		    s3.setDate(LocalDate.of(2026, 5, 28));
		    s3.setStartTime(LocalTime.of(11, 30));
		    s3.setEndTime(LocalTime.of(12, 00));
		    s3.setCoursePeriod(30);

		    schedules.add(s1);
		    schedules.add(s2);
		    schedules.add(s3);

		    course.setClassRoomSchedule(schedules);

		    model.addAttribute("course", course);
		    System.out.println("test1用controller-get側："+schedules);
		    return "test";
		}
		@PostMapping("/test")
		public String testcontInsertCourse(
				@Validated Course course,
								Errors errors,
								Model model) {
			if(errors.hasErrors()) {
				//model.addAttribute("room", classRoomService.servSelectRoomAll());
					return "test";
			}

			try {
				courseService.servInsertCourse(course);
				model.addAttribute("statusMessage","講座を登録しました");
				return "/menu";

			}catch(IllegalArgumentException e) {
				model.addAttribute("errorMessage",e.getMessage());
				return "test";
			}
		}
		@GetMapping("/test2")
		public String showAddFormTest2(Model model) {
		    Course course = new Course();
		    List<ClassRoomSchedule> schedules = new ArrayList<>();

		    // 3日分の初期データを詰める
//		    for (int i = 0; i < 3; i++) {
//		        ClassRoomSchedule s = new ClassRoomSchedule();
//		        s.setClassRoomId(1);
//		        s.setCoursePeriod(30);
//		        schedules.add(s);
//		    }
		    course.setClassRoomSchedule(schedules);
		    model.addAttribute("course", course);

		    System.out.println("test2用controller-get側："+schedules);
		    return "test2"; // test2.html を呼び出す
		}
		@PostMapping("/test2")
		public String test2contInsertCourse(
		        @Validated Course course,
		        Errors errors,
		        Model model) {

		    if (errors.hasErrors()) {
		        return "test2";
		    }

		    try {
		        // ここからServiceのロジックが走り、コンソールにDB取得結果が出力されます
		        courseService.servInsertCourse(course);
		        model.addAttribute("statusMessage", "講座を登録しました");
		        return "/menu";

		    } catch (IllegalArgumentException e) {
		        model.addAttribute("errorMessage", e.getMessage());
		        return "test2";
		    }
		}
	// ///////////////////////////////////////////////////
		@GetMapping("/roomSchedules")
//	@ResponseBody
//		public Map<String,Set<String>>constScheduleMap(Model model){
			public String contSelectClassRoomScheduleAll(
					@RequestParam(name="baseDate",required = false)String baseDateStr,
				//@RequestParam(name="week",required = false)Integer week,
					Model model){//@RequestParam(required = false) で基準日を受け取れるようになる

			LocalDate baseDate=(baseDateStr==null||baseDateStr.isEmpty())
					?LocalDate.now():LocalDate.parse(baseDateStr);

			OccupiedRoomSchedule occupiedMap=courseService.servSelectClassRoomScheduleAll(baseDate);

			model.addAttribute("occupiedMap",occupiedMap);//7日分の部屋占有データ
			model.addAttribute("baseDate",baseDate);//基準日を含む7日分の日付データ
			//前週来週のデータを事前に渡しておくと戻り値のif()判定が必要なくなる
			model.addAttribute("prevDate",baseDate.minusWeeks(1).toString());
			model.addAttribute("nextDate",baseDate.plusWeeks(1).toString());


//		Map<String,Set<String>>scheduleMap=courseService.servSelectClassRoomScheduleAll();
				/*
				public List<ClassRoomSchedule>contSelectClassRoomScheduleAll(Model model){
				for(ClassRoomSchedule rs:roomSchedules) {
					String classRoom=rs.getClassRoom().getClassRoom();
					String key=rs.getDate()+"_"+rs.getStartTime();
					scheduleMap.computeIfAbsent(classRoom, k -> new HashSet<>())
					.add(key);
				}*/
				/*
				Map<String,String>scheduleMap=new HashMap<>();
				for(ClassRoomSchedule rs:roomSchedules) {
				String key=rs.getDate()+"_"+rs.getStartTime();
				String classRoom=rs.getClassRoom().getClassRoom();
				scheduleMap.put(key,classRoom);
				}*/
				System.out.println("--利用状況--controller側取得--"+occupiedMap);

				return "roomSchedules";

		}

		@GetMapping("/cancelledSchedule")
		//@ResponseBody
		public String contCancelledSchedule(//(@ModelAttribute("classRoomSchedule")
					@RequestParam(name = "searchType", defaultValue = "lateEnrollment") String searchType,
					@RequestParam(name="page",defaultValue = "1")Integer page,
					Model model) {

					List<Course>courses=courseService.servSelectCourseByPage(page,searchType);
					double totalPages=courseService.servSelectTotalPages(searchType);

					model.addAttribute("courses",courses);
					model.addAttribute("searchType", searchType);
					model.addAttribute("page", page);
					model.addAttribute("totalPages", (int)totalPages);

				return "cancelledCourseList";
	}
		@GetMapping("/cancelledSchedule/{courseId}")
		public String contCancelledScheduleRequestByCourseId(
				@PathVariable String courseId,
				@RequestParam(name = "searchType", defaultValue = "lateEnrollment") String searchType,
				@RequestParam(defaultValue = "1") Integer page,

				Model model	) {

				List<Course> course=courseService.servSellectCourseByCourseId(courseId);
				RemainingCourseData rcData=
						memberService.servSelectRemainingCourseData(courseId);
				model.addAttribute("rcData",rcData);
				model.addAttribute("course",course);
				model.addAttribute("searchType", searchType);
				model.addAttribute("page", page);

				// /////////////////
				Map<Integer,RefundSummary> refundSummaryMap =
						refundService.calculateRefundSummary(courseId);

				//データ取得
				//以下public class RefundServiceImplへ
/*				List<ScheduleAccountingDetail> rawList =
						courseService.servSelectScheduleAccountingDetails(courseId);
				//chMemberId ごとにグループ化
				Map<Integer, List<ScheduleAccountingDetail>> groupedCourses = rawList.stream()
				    .collect(Collectors.groupingBy(
				        ScheduleAccountingDetail::getChMemberId,
				        LinkedHashMap::new, // 順序を維持
				        Collectors.toList()
				    ));
				// 今日の日付（開始前/開始後の判定用）
				LocalDateTime today = LocalDateTime.now();

				//画面表示・計算用にグループごとの集計情報を保持する Map を作成
				Map<Integer, Map<String, Object>> refundSummaryMap = new LinkedHashMap<>();

				for (Map.Entry<Integer, List<ScheduleAccountingDetail>>
					entry : groupedCourses.entrySet()) {
				    Integer memberId = entry.getKey();
				    List<ScheduleAccountingDetail> details = entry.getValue();
				    ScheduleAccountingDetail firstItem = details.get(0);

				 // 今日以降（未受講）のコマデータだけを抽出したリストを作成
				    List<ScheduleAccountingDetail> remainingSchedules = details.stream()
				            .filter(s -> {
				              // crsDate と crsStartTime を結合して LocalDateTime を作成
				              LocalDateTime scheduleDateTime = LocalDateTime.of(s.getCrsDate(), s.getCrsStartTime());
				              // 現在日時より前でない（＝現在時刻以降）コマを残す
				              return !scheduleDateTime.isBefore(today);
				          })
				            .collect(Collectors.toList());

				    // 全コマ数と未受講コマ数（今日以降のコマ）をカウント
				    long totalCount = details.size();
				    long remainingCount = remainingSchedules.size(); // 今日以降のコマ

				    // 判定フラグ
				    boolean isBeforeStart = (remainingCount == totalCount); // 1度も開始していないか
				    boolean isFinished = (remainingCount == 0);             // すべて終了しているか

				    // 1コマあたりの受講料単価
				    int unitTuitionFee = firstItem.getCTuitionFee();

				    // 返金対象の受講料計算（未受講コマ数 × 単価）
				    int refundTuitionFee = (int) (remainingCount * unitTuitionFee);

				    // 返金対象の教材費計算（開始前のみ全額、開始後は0円）
				    int refundMaterialFee = isBeforeStart ? firstItem.getChPaidMaterialFee() : 0;

				    // 返金合計額
				    int totalRefundAmount = refundTuitionFee + refundMaterialFee;

				    // 画面渡し用の Map に格納
				    Map<String, Object> summary = new HashMap<>();
				    summary.put("details", details);
				    summary.put("firstItem", firstItem);// 1コマあたりの受講料単価
				    summary.put("totalCount", totalCount);
				    summary.put("remainingCount", remainingCount);
				    summary.put("remainingSchedules", remainingSchedules);
				    summary.put("isBeforeStart", isBeforeStart);
				    summary.put("isFinished", isFinished);
				    summary.put("refundTuitionFee", refundTuitionFee);
				    summary.put("refundMaterialFee", refundMaterialFee);
				    summary.put("totalRefundAmount", totalRefundAmount);

				    refundSummaryMap.put(memberId, summary);

				}
*/
//				System.out.println("GET側******"+refundSummaryMap);
//				System.out.println("GET側******"+rcData);
//				System.out.println("GET側******"+course);

				model.addAttribute("refundSummaryMap", refundSummaryMap);


				return "cancelledCourseInfo";
	}
		@PostMapping("/cancelledSchedule/{courseId}")
		public String contCancelledScheduleByCourseId(
//				Errors errors,
				@PathVariable("courseId") String courseId,
				@RequestParam(name = "searchType", defaultValue = "lateEnrollment") String searchType,
				@RequestParam(defaultValue = "1") Integer page,
//				@RequestParam("courseId")String courseId,
				RedirectAttributes rd,
				Model model	) {

			Map<Integer,RefundSummary> refundSummaryMap =
					refundService.calculateRefundSummary(courseId);

//			System.out.println("PUT側******"+refundSummaryMap);
			if(refundSummaryMap==null || refundSummaryMap.isEmpty()) {
	
				System.out.println("PUT側******受講生なし"+courseId);
			//受講生なし講座中止メソッド
			boolean isSuccess=refundService.servCancellCourse(courseId);
				
				if(isSuccess) {
					rd.addFlashAttribute("statusMessage","講座中止手続きを承りました");

				}else {
					rd.addFlashAttribute("errorMessage","講座中止手続き対象講座がみつかりません");
				}
			}
			
				System.out.println("PUT側******受講生あり"+courseId);
			//受講生あり講座中止メソッド
			boolean isSuccess=refundService.servCancellCourseWithMemberId(courseId);
			
				if(isSuccess) {
					rd.addFlashAttribute("statusMessage","講座中止手続きを承りました");

				}else {
					rd.addFlashAttribute("errorMessage","講座中止手続き対象講座がみつかりません");
				}

			rd.addAttribute("page", page);
	    rd.addAttribute("searchType", searchType);

			return "redirect:/cancelledSchedule/" + courseId;
		}



}
