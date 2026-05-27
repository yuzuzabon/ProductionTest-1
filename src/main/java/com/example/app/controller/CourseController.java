package com.example.app.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.service.ClassRoomService;
import com.example.app.service.CourseService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor

public class CourseController {

		private final CourseService courseService;
		private final ClassRoomService classRoomService;
		private final int NUM_PER_PAGE=5;//1ページに表示される件数
		//private final ClassRoomScheduleService classRoomScheduleService;

		@ModelAttribute("classRoomList")
		public List<ClassRoomService> populateClassRooms() {
		return classRoomService.servSelectRoomAll();

	}

		@GetMapping("/menu")
		public String showMenu() {
			return "menu";
		}

		@GetMapping("/show")
		//講座全件取得
//		public String contSelectCourseAll(Model model) {
//			model.addAttribute("courses",service.servSelectCourseAll());
		
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
		public String contInsertCourse(
				@Validated Course course,
								Errors errors,
								Model model) {
			if(errors.hasErrors()) {
				//model.addAttribute("room", classRoomService.servSelectRoomAll());
					return "insertCourse";
			}

			try {
				
				courseService.servInsertCourse(course);
				model.addAttribute("status","講座を登録しました");
				return "/menu";

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
		    System.out.println(schedules);
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
				model.addAttribute("status","講座を登録しました");
				return "/menu";

			}catch(IllegalArgumentException e) {
				model.addAttribute("errorMessage",e.getMessage());
				return "test";
			}
		}
		
		/*@PostMapping("/test2") // 現在お使いのURL（マッピングアノテーション）に合わせてください
		public String testcontInsertCourse(
				@Validated Course course,Model model) {
						
				
		    // 1. schedules(3件) から [2026-05-26, 2026-05-27, 2026-05-28] という日付のリストを作る
		    List<LocalDate> dateList = schedules.stream()
		        .map(ClassRoomSchedule::getDate)
		        .filter(Objects::nonNull)
		        .collect(Collectors.toList());
		        
		    // 2. 1発目のデータから教室IDを取得（どれも同じ教室IDが入っているため、0番目から取得）
		    Integer classRoomId = schedules.get(0).getClassRoomId();

		    // 3. Serviceを呼び出して、DBから該当する既存データを一括取得
		    List<ClassRoomSchedule> existingSchedules = 
		        courseService.servSelectRegisteredSchedule(classRoomId, dateList);

		    // 4. HTML側で表示するために、取得した「既存データ」をModelに登録
		    model.addAttribute("testSchedules", existingSchedules);

		    // 5. 確認画面、または現在の入力画面などのHTML名（例: "register_confirm"）
		    return null;
		}*/
}
