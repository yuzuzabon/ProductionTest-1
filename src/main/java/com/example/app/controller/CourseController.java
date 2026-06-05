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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.domain.OccupiedRoomSchedule;
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

		@GetMapping("/show/{id}")
			public String contSelectCourseById(
					@PathVariable String id,
					Model model		) {
					List<Course> course=courseService.servSellectCourseById(id);
					model.addAttribute("course",course);
					System.out.println("******"+course);
					return "courseInfo";
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
								RedirectAttributes redirectAttributes) {
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
				redirectAttributes.addFlashAttribute("statusMessage", "講座を登録しました");
				return "redirect:/show/"+courseId;
			}else{
				model.addAttribute("errorMessage", "重複があります");
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
				model.addAttribute("status","講座を登録しました");
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
		        model.addAttribute("status", "講座を登録しました");
		        return "/menu";

		    } catch (IllegalArgumentException e) {
		        model.addAttribute("errorMessage", e.getMessage());
		        return "test2";
		    }
		}
	// ///////////////////////////////////////////////////
		@GetMapping("/roomSchedules2")
//	@ResponseBody
//		public Map<String,Set<String>>constScheduleMap(Model model){
			public String constScheduleMap(
					@RequestParam(name="baseDate",required = false)String baseDateStr,
					@RequestParam(name="week",required = false)Integer week,
					Model model){//@RequestParam(required = false) で基準日を受け取れるようになる
		
			LocalDate baseDate=(baseDateStr==null||baseDateStr.isEmpty())
					?LocalDate.now():LocalDate.parse(baseDateStr);
			
			if(week !=null) {
				if(week==1) {
					baseDate = baseDate.minusWeeks(1);
				}else if(week==2) {
					baseDate = baseDate.plusWeeks(1);
					}
			}
			
			
			OccupiedRoomSchedule occupiedMap=courseService.servSelectClassRoomScheduleAll(baseDate);		
			
			model.addAttribute("occupiedMap",occupiedMap);
			model.addAttribute("baseDate",baseDate);
			
			
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
				System.out.println(occupiedMap);
			
				return "roomSchedules2";

		}

}
