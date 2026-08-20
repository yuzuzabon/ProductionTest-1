package com.example.app.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.example.app.domain.ScheduleAccountingDetail;
import com.example.app.service.CourseService;
import com.example.app.service.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
//@NoArgsConstructor

public class MemberController {
		private final MemberService memberService;
		private final CourseService courseService;
//		private final int NUM_PER_PAGE=5;

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
					@RequestParam(name = "searchType", defaultValue = "lateEnrollment") String searchType,
//					@RequestParam(name = "searchType", defaultValue = "all") String searchType,
					@RequestParam(name="page",defaultValue = "1")Integer page,
					Model model) {

					setMemberInfo(id,model);
					// 以下をメソッド化 private List<CourseHistory> setMemberInfo(Integer id,Model model)
//					Member member= memberService.servSelectMemberById(id);
//					model.addAttribute("member",member);
//
//					List<CourseHistory>history=
//							memberService.servSellectCourseHistoryById(id);
//					model.addAttribute("history",history);

					model.addAttribute("courses",
							courseService.servSelectCourseByPage(page, searchType));
					model.addAttribute("page",page);
					model.addAttribute("totalPages",
							courseService.servSelectTotalPages(searchType));

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
// test -------
//			System.out.println("controller---"+course);

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
			//初回からの受講or途中受講判定
			boolean isFullCourse=memberService.servisFullCourseEnrollment(courseId);

			//初回からの申し込み処理
			if(isFullCourse) {
				boolean isSuccess=memberService.servApplyCourse(id,courseId);

				if(isSuccess) {
					rd.addFlashAttribute("statusMessage","お申し込みを承りました");

				}else {
					rd.addFlashAttribute("errorMessage","同じ講座にお申し込み済みです");
				}
			}else {
				boolean isValid=memberService.servisValidLateEnrollment(courseId);
				if(isValid) {

			//途中申し込みservRemainingApplyCourse
				boolean isSuccess=memberService.servRemainingApplyCourse(id,courseId);
				if(isSuccess) {
					rd.addFlashAttribute("statusMessage","お申し込みを承りました");

				}else {
					rd.addFlashAttribute("errorMessage","同じ講座にお申し込み済みです");
				}

				}else {
					rd.addFlashAttribute("errorMessage","この講座は途中受講できません");
				}

			}
				//リダイレクトパラメータ設定
					rd.addAttribute("page",page);
					rd.addAttribute("courseId",courseId);
						//return "redirect:/member1/"+id+"?courseId="+courseId;

						return "redirect:/memberjoin/"+id;

		}
		@GetMapping("/cancelledMembers")
		public String contSelectCancelledMemberAll(Model model) {
			model.addAttribute("members",memberService.servSelectMemberAll());
			return "cancelledMembers";
	}
		@GetMapping("/cancelledMember/{id}")
		//@ResponseBody
			public String contSelectCancellMemberById(
					@PathVariable("id") Integer id,
					@RequestParam(name = "searchType", defaultValue = "lateEnrollment") String searchType,
//					@RequestParam(name = "searchType", defaultValue = "all") String searchType,
					@RequestParam(name="page",defaultValue = "1")Integer page,
					Model model) {

					setMemberInfo(id,model);


					//データ取得
					List<ScheduleAccountingDetail> rawList = memberService.servSelectCancellMemberById(id);
					//chCourseId ごとにグループ化
					Map<String, List<ScheduleAccountingDetail>> groupedCourses = rawList.stream()
					    .collect(Collectors.groupingBy(
					        ScheduleAccountingDetail::getChCourseId,
					        LinkedHashMap::new, // 順序を維持
					        Collectors.toList()
					    ));
					// 今日の日付（開始前/開始後の判定用）
					LocalDateTime today = LocalDateTime.now();

					//画面表示・計算用にグループごとの集計情報を保持する Map を作成
					Map<String, Map<String, Object>> refundSummaryMap = new LinkedHashMap<>();

					for (Map.Entry<String, List<ScheduleAccountingDetail>> entry : groupedCourses.entrySet()) {
					    String courseId = entry.getKey();
					    List<ScheduleAccountingDetail> details = entry.getValue();
					    ScheduleAccountingDetail firstItem = details.get(0);

					 // 今日以降（未受講）のコマデータだけを抽出したリストを作成
					    List<ScheduleAccountingDetail> remainingSchedules = details.stream()
//					            .filter(s -> !s.getCrsDate().isBefore(today)) // 今日以降のコマ
//					            .collect(Collectors.toList());
					    .filter(s -> {
					    	if (s.getCrsDate() == null || s.getCrsStartTime() == null) {
	                return false;
	            }
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
					    int refundTuitionFee = (int) remainingCount * unitTuitionFee;

					    // 返金対象の教材費計算（開始前のみ全額、開始後は0円）
					    int refundMaterialFee = isBeforeStart ? firstItem.getChPaidMaterialFee() : 0;

					    // 返金合計額
					    int totalRefundAmount = refundTuitionFee + refundMaterialFee;

					    // 画面渡し用の Map に格納
					    Map<String, Object> summary = new HashMap<>();
					    summary.put("details", details);
					    summary.put("firstItem", firstItem);
					    summary.put("totalCount", totalCount);
					    summary.put("remainingCount", remainingCount);
					    summary.put("remainingSchedules", remainingSchedules);
					    summary.put("isBeforeStart", isBeforeStart);
					    summary.put("isFinished", isFinished);
					    summary.put("refundTuitionFee", refundTuitionFee);
					    summary.put("refundMaterialFee", refundMaterialFee);
					    summary.put("totalRefundAmount", totalRefundAmount);

					    refundSummaryMap.put(courseId, summary);
					}
					model.addAttribute("refundSummaryMap", refundSummaryMap);
					return "cancelledMemberWithCourseList";

		}
}
