package com.example.app.controller;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.service.ClassRoomService;
import com.example.app.service.CourseService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor

public class CourseController {

		private final CourseService service;
		private final ClassRoomService classRoomService;
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
		public String contSelectCourseAll(Model model) {
			model.addAttribute("course",service.servSelectCourseAll());
		//System.out.println(service.servSelectCourseAll());
			return "courseList";
		}
		@GetMapping("/add")
		public String showInsertCourse(Model model) {
				model.addAttribute("course",new Course());
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
			ClassRoomSchedule schedule = course.getClassRoomSchedule();
			if (schedule != null && schedule.getStartTime() != null && schedule.getCorsePeriod() != null) {
				LocalTime start = schedule.getStartTime();
        int period = schedule.getCorsePeriod();
        LocalTime end = start.plusMinutes(period);

        // 計算した終了時間をセットする
        schedule.setEndTime(end);
        System.out.println(course);
			}
				service.servInsertCourse(course);

			model.addAttribute("status","講座を登録しました");

		return "/menu";
		}

}
