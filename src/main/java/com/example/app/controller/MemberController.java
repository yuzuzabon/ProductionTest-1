package com.example.app.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.app.domain.Course;
import com.example.app.domain.Member;
import com.example.app.service.CourseService;
import com.example.app.service.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
//@NoArgsConstructor

public class MemberController {
		private final MemberService memberService;
		private final CourseService courseService;
		private final int NUM_PER_PAGE=5;

		@GetMapping("/members")
			public String contSelectMemberAll(Model model) {
				model.addAttribute("members",memberService.servSelectMemberAll());
				return "members";
		}
		@GetMapping("/search")
			public String contSelectMemberByWord(
				@RequestParam(required = false) Integer id,
				@RequestParam(required = false) String name,
				Model model) {
				model.addAttribute("members",memberService.servSelectMemberByWord(id, name));
				return "members";
		}
		
		@GetMapping("/member/{id}")
		//@ResponseBody
			public String contSelectMemberById(
					@PathVariable Integer id,
					@RequestParam(name="page",defaultValue = "1")Integer page,
				
					Model model) {
			
					Member member= memberService.servSelectMemberById(id);
					model.addAttribute("member",member);
												
					model.addAttribute("courses",
							courseService.servSelectCourseByPage(page, NUM_PER_PAGE));
					model.addAttribute("page",page);
					model.addAttribute("totalPages",
							courseService.servSelectTotalPages(NUM_PER_PAGE));
				
					return "memberWithCourseList";
					
		}
		@GetMapping("/member1/{id}")
	
			public String contSelectMemberByIdWithCourseId(
				@PathVariable Integer id,
				@RequestParam(name="courseId")String courseId,
				Model model	) {
			
				Member member= memberService.servSelectMemberById(id);
				model.addAttribute("member",member);

				List<Course> course=courseService.servSellectCourseByCourseId(courseId);
				model.addAttribute("course",course);
		
					return "memberWithCourseInfo";
	}
		
}
