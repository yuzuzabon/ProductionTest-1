package com.example.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.app.domain.Course;
import com.example.app.domain.RefundSummary;
import com.example.app.domain.RemainingCourseData;
import com.example.app.service.CourseService;
import com.example.app.service.MemberService;
import com.example.app.service.RefundService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RefundController {

		private final CourseService courseService;
		private final MemberService memberService;
		private final MemberController memberController;
		private final RefundService refundService;
		
		@GetMapping("/cancelledSchedule")
		//@ResponseBody
		public String contCancelledSchedule(//(@ModelAttribute("classRoomSchedule")
					@RequestParam(name = "searchType", defaultValue = "refund") String searchType,
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
				@RequestParam(name = "searchType", defaultValue = "refund") String searchType,
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
				@RequestParam(name = "searchType", defaultValue = "refund") String searchType,
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
			} else {

				System.out.println("PUT側******受講生あり"+courseId);
			//受講生あり講座中止メソッド
			boolean isSuccess=refundService.servCancellCourseWithMemberId(courseId);

				if(isSuccess) {
					rd.addFlashAttribute("statusMessage","講座中止手続きを承りました");

				}else {
					rd.addFlashAttribute("errorMessage","講座中止手続き対象講座がみつかりません");
				}
			}
			rd.addAttribute("page", page);
	    rd.addAttribute("searchType", searchType);

			return "redirect:/cancelledSchedule/" + courseId;
		}

		@PostMapping("/cancelledSession/{courseId}")
		public String contCancelledSessionByCourseId(
//				Errors errors,
				@PathVariable("courseId") String courseId,
				@RequestParam("id") Integer targetId,
				@RequestParam(name = "searchType", defaultValue = "refund") String searchType,
				@RequestParam(defaultValue = "1") Integer page,
//				@RequestParam("courseId")String courseId,
				RedirectAttributes rd,
				Model model	) {

			Map<Integer,RefundSummary> refundSummaryMap =
					refundService.calculateRefundSummary(courseId);

			System.out.println("休講PUT側******"+refundSummaryMap);
			if(refundSummaryMap==null || refundSummaryMap.isEmpty()) {

				System.out.println("PUT側******受講生なし"+courseId);
				System.out.println("休講対象のSchedule ID: " + targetId);
			//受講生なし講座休講メソッド
			boolean isSuccess=refundService.servCancellSession(courseId,targetId);

				if(isSuccess) {
					rd.addFlashAttribute("statusMessage","講座休講手続きを承りました");

				}else {
					rd.addFlashAttribute("errorMessage","講座休講手続き対象講座がみつかりません");
				}
			} else {

			System.out.println("PUT側******受講生あり"+courseId);
			//受講生あり講座休講メソッド
			boolean isSuccess=refundService.servCancellSessionWithMemberId(courseId,targetId);

			if(isSuccess) {
				rd.addFlashAttribute("statusMessage","講座休講手続きを承りました");

				}else {
				rd.addFlashAttribute("errorMessage","講座休講手続き対象講座がみつかりません");
				}
			}
			rd.addAttribute("page", page);
	    rd.addAttribute("searchType", searchType);

			return "redirect:/cancelledSchedule/" + courseId;

		}
		
		
		@GetMapping("/cancelledMembers")
		public String contSelectCancelledMemberAll(Model model) {
			model.addAttribute("members",memberService.servSelectMemberAll());
			return "cancelledMembers";
	}
		@GetMapping("/cancelledMember/{memberId}")
		//@ResponseBody
			public String contSelectCancellMemberById(
					@PathVariable("memberId") Integer memberId,
//					@RequestParam(name = "searchType", defaultValue = "lateEnrollment") String searchType,
//					@RequestParam(name = "searchType", defaultValue = "all") String searchType,
					@RequestParam(name="page",defaultValue = "1")Integer page,
					Model model) {

					memberController.setMemberInfo(memberId,model);
					
					Map<String, RefundSummary> refundSummaryMap = 
							refundService.calculateRefundSummaryForMember(memberId);

/*					
					//データ取得
					List<ScheduleAccountingDetail> rawList = memberService.servSelectCancellMemberById(memberId);
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
		*/			
					model.addAttribute("refundSummaryMap", refundSummaryMap);
					return "cancelledMemberWithCourseList";

		}
		
		// /////////受講生事由の払い戻し/////////
		@PostMapping("/cancelledCourse/{memberId}/{courseId}")
			public String contCancellCourseMemberById(
					@PathVariable("memberId") Integer memberId,
					@PathVariable("courseId") String courseId,
//				@RequestParam(name = "searchType", defaultValue = "lateEnrollment") String searchType,
//				@RequestParam(name = "searchType", defaultValue = "all") String searchType,
//					@RequestParam(name="page",defaultValue = "1")Integer page,
					RedirectAttributes rd,
					Model model) {
			
			boolean isSuccess=refundService.servRefundCourseWithMemberId(courseId,memberId);		
			
			if(isSuccess) {
				rd.addFlashAttribute("statusMessage","講座払い戻し手続きを承りました");

				}else {
				rd.addFlashAttribute("errorMessage","講座休講手続き対象講座がみつかりません");
				}
			
			return "redirect:/cancelledMember/" + memberId;
		}
}
