package com.example.app.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.app.domain.Course;
import com.example.app.service.CourseService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor

public class CourseController {

		private final CourseService service;

		@GetMapping("/menu")
		public String showMenu() {
			return "menu";
		}

		@GetMapping("/show")
		public String contSelectCourseAll(Model model) {
			model.addAttribute("course",service.servSelectCourseAll());

			return "courseList";
		}
		@GetMapping("/add")
		public String showInsertForm(Model model) {
				model.addAttribute("course",new Course());
				return "insertCourse";
		}
		@PostMapping("/add")
		public String contInsertCourse(
				@Valid Course course,
								Errors errors,
								Model model) {
			if(errors.hasErrors()) {
					return "insertCourse";
			}
			service.servInsertCourse(course);
			model.addAttribute("status","講座を登録しました");

		return "/menu";
		}

}
