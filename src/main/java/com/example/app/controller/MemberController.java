package com.example.app.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.app.domain.Course;
import com.example.app.domain.CourseHistory;
import com.example.app.domain.Member;
import com.example.app.domain.RemainingCourseData;
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

		private List<CourseHistory> setMemberInfo(Integer id,Model model) {
			Member member= memberService.servSelectMemberById(id);
			model.addAttribute("member",member);

			List<CourseHistory>history=
					memberService.servSellectCourseHistoryById(id);
			model.addAttribute("history",history);

				return history;
		}

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

					setMemberInfo(id,model);
					// 以下をメソッド化
//					Member member= memberService.servSelectMemberById(id);
//					model.addAttribute("member",member);
//
//					List<CourseHistory>history=
//							memberService.servSellectCourseHistoryById(id);
//					model.addAttribute("history",history);

					model.addAttribute("courses",
							courseService.servSelectCourseByPage(page, NUM_PER_PAGE));
					model.addAttribute("page",page);
					model.addAttribute("totalPages",
							courseService.servSelectTotalPages(NUM_PER_PAGE));
					
					return "memberWithCourseList";

		}
		@GetMapping("/memberjoin/{id}")

			public String contSelectMemberByIdWithCourseId(
				@PathVariable Integer id,
				@RequestParam(name="courseId")String courseId,
				@RequestParam(defaultValue = "1") Integer page,
				Model model	) {

				setMemberInfo(id,model);

				List<Course> course=
						courseService.servSellectCourseByCourseId(courseId);
				model.addAttribute("course",course);

				List<CourseHistory>history=setMemberInfo(id,model);
				RemainingCourseData rcData=
						memberService.servSelectRemainingCourseData(courseId);
				model.addAttribute("rcData",rcData);

				boolean isApplied=history.stream()
						.map(CourseHistory::getCourseId)
						.anyMatch(courseIdStr -> courseIdStr.equals(courseId));
				model.addAttribute("isApplied",isApplied);
				model.addAttribute("page", page);

					return "memberWithCourseInfo";
	}
		@PostMapping("/apply")
	//@ResponseBody
			public String contApplication(
					@RequestParam(name="id")Integer id,
					@RequestParam(name="courseId")String courseId,
					@RequestParam(defaultValue = "1") Integer page,
					//@RequestParam Integer page,
					RedirectAttributes rd) {
				rd.addAttribute("page", page);
			boolean isSuccess=memberService.servApplyCourse(id,courseId);

			if(isSuccess) {
				rd.addFlashAttribute("statusMessage","お申し込みを承りました");
				rd.addAttribute("courseId",courseId);
					return "redirect:/memberjoin/"+id;
			}else {
				rd.addFlashAttribute("errorMessage","同じ講座にお申し込み済みです");
				rd.addAttribute("courseId",courseId);
				//return "redirect:/member1/"+id+"?courseId="+courseId;
					return "redirect:/memberjoin/"+id;
			}

		}
}
