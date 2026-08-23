package com.example.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.domain.CourseSales;
import com.example.app.domain.CourseSalesRefund;
import com.example.app.domain.RefundSummary;
import com.example.app.domain.ScheduleAccountingDetail;
import com.example.app.mapper.CourseMapper;
import com.example.app.mapper.MemberMapper;
import com.example.app.mapper.MemberScheduleStatusMapper;
import com.example.app.mapper.RefundMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundServiceImpl implements RefundService{

		private final CourseService courseService;
		private final MemberService memberService;
		private final MemberScheduleStatusMapper scheduleStatusMapper;
		private final RefundMapper refundMapper;
		private final CourseMapper courseMapper;
		private final MemberMapper memberMapper;

		@Override
		public Map<Integer, RefundSummary> calculateRefundSummary(String courseId) {
		//get用：画面表示用の集計データ取得
			List<ScheduleAccountingDetail> rawList =
				scheduleStatusMapper.selectScheduleAccountingDetails(courseId);

		// ★ログを追加して確認してみてください
	    System.out.println("★引数のcourseId: [" + courseId + "]");
	    System.out.println("★rawListの件数: " + (rawList != null ? rawList.size() : "null"));

		//chMemberId ごとにグループ化
			Map<Integer, List<ScheduleAccountingDetail>> groupedCourses = rawList.stream()
				.collect(Collectors.groupingBy(
				ScheduleAccountingDetail::getChMemberId,
						LinkedHashMap::new, // 順序を維持
						Collectors.toList()
						));

			LocalDateTime today = LocalDateTime.now();
			Map<Integer, RefundSummary>refundSummaryMap = new LinkedHashMap<>();

		//各受講生の返金額計算ループ
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
		  						boolean isFuture = !scheduleDateTime.isBefore(today);
		  						// ステータスの判定（すでに休講または中止になっていないか）
		  						// ※「CANCELLED_SESSION」および「CANCELLED_COURSE」を除外対象にする
		  						String status = s.getCrsScheduleStatus();
		  						boolean isNotCancelled = !"CANCELLED_SESSION".equals(status)
		  																	&& !"CANCELLED_COURSE".equals(status);

		  						// 両方の条件を満たす（未来の日程 且つ まだ休講/中止されていない）コマだけを残す
		  						return isFuture && isNotCancelled;
		              //return !scheduleDateTime.isBefore(today);
		          })
		            .collect(Collectors.toList());

				System.out.println("*****検証用"+remainingSchedules);

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
		    RefundSummary summary = new RefundSummary();
		    summary.setDetails(details);
		    summary.setFirstItem(firstItem);// 1コマあたりの受講料単価
		    summary.setTotalCount(totalCount);
        summary.setRemainingCount(remainingCount);
        summary.setRemainingSchedules(remainingSchedules);
        summary.setBeforeStart(isBeforeStart);
        summary.setFinished(isFinished);
        summary.setRefundTuitionFee(refundTuitionFee);
        summary.setRefundMaterialFee(refundMaterialFee);
        summary.setTotalRefundAmount(totalRefundAmount);

		    refundSummaryMap.put(memberId, summary);

			}
			System.out.println("休講test*****"+refundSummaryMap);

			return refundSummaryMap;

		}

		public Map<Integer, RefundSummary> calculateRefundSummaryForSession
																			(String courseId, Integer targetId) {

	    // 1. 全コマの会計明細データを取得
	    List<ScheduleAccountingDetail> rawList =
	        scheduleStatusMapper.selectScheduleAccountingDetails(courseId);

	    if (rawList == null || rawList.isEmpty()) {
	        return Collections.emptyMap();
	    }

	    // 2. chMemberId ごとにグループ化
	    Map<Integer, List<ScheduleAccountingDetail>> groupedCourses = rawList.stream()
	        .collect(Collectors.groupingBy(
	            ScheduleAccountingDetail::getChMemberId,
	            LinkedHashMap::new,
	            Collectors.toList()
	        ));

	    LocalDateTime today = LocalDateTime.now();
	    Map<Integer, RefundSummary> refundSummaryMap = new LinkedHashMap<>();

	    // 3. 受講生ごとの返金額計算ループ
	    for (Map.Entry<Integer, List<ScheduleAccountingDetail>>
	    	entry : groupedCourses.entrySet()) {
	        Integer memberId = entry.getKey();
	        List<ScheduleAccountingDetail> details = entry.getValue();
	        ScheduleAccountingDetail firstItem = details.get(0);

	        // 【ポイント1】今回の休講対象コマ（targetId）のみを抽出
	        List<ScheduleAccountingDetail> targetScheduleList = details.stream()
	            .filter(s -> targetId.equals(s.getCrsId())) // ※プロパティ名は環境に合わせて変更してください
	            .collect(Collectors.toList());

	        // 対象のコマが存在しない（または既に受講済み等の）場合はスキップ
	        if (targetScheduleList.isEmpty()) {
	            continue;
	        }

	        // 【ポイント2】全体の未受講コマ数（今回の休講判定用）
	        long totalCount = details.size();

	        // 今日の日時以降で、かつ「未休講・未中止」の有効なコマ数をカウント
	        long activeRemainingCount = details.stream()
	            .filter(s -> {
	                LocalDateTime scheduleDateTime = LocalDateTime.of(s.getCrsDate(), s.getCrsStartTime());
	                String status = s.getCrsScheduleStatus();
	                return !scheduleDateTime.isBefore(today)
	                    && !"CANCELLED_SESSION".equals(status)
	                    && !"CANCELLED_COURSE".equals(status);
	            })
	            .count();

	        // 1コマあたりの受講料単価
	        int unitTuitionFee = firstItem.getCTuitionFee();

	        // 【ポイント3】返金対象受講料は「休講にする 1コマ分」のみ
	        int refundTuitionFee = unitTuitionFee;

	        // 【ポイント4】教材費返金判定
	        // 単発休講では受講自体は継続するため、原則「教材費返金は0円」
	        int refundMaterialFee = 0;

	        // 返金合計額
	        int totalRefundAmount = refundTuitionFee + refundMaterialFee;

	        // 画面・後続処理渡し用の Summary に格納
	        RefundSummary summary = new RefundSummary();
	        summary.setDetails(details);
	        summary.setFirstItem(firstItem);
	        summary.setTotalCount(totalCount);
	        summary.setRemainingCount(activeRemainingCount - 1); // 今回の休講で残コマが1減る
	        summary.setRemainingSchedules(targetScheduleList);   // 今回返金対象となるコマ
	        summary.setRefundTuitionFee(refundTuitionFee);
	        summary.setRefundMaterialFee(refundMaterialFee);
	        summary.setTotalRefundAmount(totalRefundAmount);

	        refundSummaryMap.put(memberId, summary);
	    }

	    return refundSummaryMap;
	}
		@Override
		public boolean servCancellCourse(String courseId){

			List<Course>courseList=courseService.servSellectCourseByCourseId2(courseId);
				//講座情報なし判定
			 if (courseList == null || courseList.isEmpty()) {
	        return false;
	    }

			Course course=courseList.get(0);
			List<ClassRoomSchedule> schedules = course.getClassRoomSchedule();
				//スケジュール情報なし判定
			 if (schedules == null || schedules.isEmpty()) {
	        return false;
	    }

	    LocalDateTime today = LocalDateTime.now();

	    List<Integer> targetIds = schedules.stream()
          .filter(s -> {
            // 日時の判定（現在時刻以降か）Date と StartTime を結合して LocalDateTime を作成
            LocalDateTime scheduleDateTime = LocalDateTime.of(s.getDate(), s.getStartTime());
            // 現在日時より前でない（＝現在時刻以降）コマを残す
            boolean isFuture = !scheduleDateTime.isBefore(today);
            // ステータスの判定（すでに休講または中止になっていないか）
            // ※「CANCELLED_SESSION」および「CANCELLED_COURSE」を除外対象にする
            String status = s.getScheduleStatus();
            boolean isNotCancelled = !"CANCELLED_SESSION".equals(status)
                                  && !"CANCELLED_COURSE".equals(status);

            // 両方の条件を満たす（未来の日程 且つ まだ休講/中止されていない）コマだけを残す
            return isFuture && isNotCancelled;
            //  return !scheduleDateTime.isBefore(today);

        })
          .map(ClassRoomSchedule::getId)
          .collect(Collectors.toList());

      System.out.println("******PUT側"+targetIds);

	    // 更新対象のIDが存在する場合のみ、一括更新処理を1回だけ呼ぶ
	    if (!targetIds.isEmpty()) {
	        refundMapper.updateCrsStatusForCancellation(targetIds, "CANCELLED_COURSE");
	    }


			return true;
		}
		/**
		 *
		 */
		@Override
		public boolean servCancellCourseWithMemberId(String courseId){

			List<Course>courseList=courseService.servSellectCourseByCourseId2(courseId);
				//講座情報なし判定
				if (courseList == null || courseList.isEmpty()) {
					return false;
				}

			Course course=courseList.get(0);
			List<ClassRoomSchedule> schedules = course.getClassRoomSchedule();
			//スケジュール情報なし判定
				if (schedules == null || schedules.isEmpty()) {
					return false;
				}
			Map<Integer,RefundSummary> refundSummaryMap =
					calculateRefundSummary(courseId);
//			Set<Integer>memberIds=refundSummaryMap.keySet();
			//course_history用
				System.out.println("course_history書き込み");
				for (Map.Entry<Integer, RefundSummary> entry : refundSummaryMap.entrySet()) {
					Integer memberId = entry.getKey();
					RefundSummary summary = entry.getValue();

					int refundtuitionFee = summary.getRefundTuitionFee();   // 返金受講料
					int refundmaterialFee = summary.getRefundMaterialFee(); // 返金教材費

		    // 個別のDB更新や通知処理を実施
					System.out.println("memberId:"+memberId+" courseId:"+
		    courseId+" tuitionFee:"+refundtuitionFee+" materialFee:"+refundmaterialFee);

				// ////////course_history書き込み////////
//					if (memberId !=null || courseId.isEmpty()) {
					if (memberId != null && courseId != null && !courseId.isEmpty()) {
					refundMapper.updateCourseHistoryForRefund(memberId,courseId,refundtuitionFee,refundmaterialFee);
					}
				}
				DateTimeFormatter formatter=
						DateTimeFormatter.ofPattern("yyyyMM");//getCrsDate().format(formatter);
			// キー: "courseId_targetMonth" (例: "2026070007_2026-09")
				Map<String, CourseSalesRefund> summaryMap = new HashMap<>();

					for (RefundSummary summary : refundSummaryMap.values()) {
						List<ScheduleAccountingDetail> remainingSchedules =
								summary.getRemainingSchedules();

						// 未受講の対象講座（remainingSchedules）が存在しない場合はスキップ
						if (remainingSchedules == null || remainingSchedules.isEmpty()) {
							continue;
						}
						//受講料の集計（コマごとにループして対象月へ加算）
						for (ScheduleAccountingDetail detail : remainingSchedules) {
								if (detail.getCrsDate() != null) {

									//String courseId = detail.getChCourseId();
									String tuitionTargetMonth = detail.getCrsDate().format(formatter);
									// 複合キーの作成
									String key = courseId + "_" + tuitionTargetMonth;

									CourseSalesRefund dto = summaryMap.computeIfAbsent(
											key, k -> new CourseSalesRefund(courseId, tuitionTargetMonth)
											);
									dto.addTuitionFee(detail.getCTuitionFee());
				        }
							}

						//教材費の集計（返金対象の教材費がある場合のみ、未受講講座の初回日付へ加算）
						if(summary.getRefundMaterialFee()>0) {
						// 未受講講座の先頭コマを取得
							ScheduleAccountingDetail firstRemainingSchedule = remainingSchedules.get(0);
							if(firstRemainingSchedule.getCrsDate()!=null) {
								String materialTargetMonth = firstRemainingSchedule.getCrsDate().format(formatter);
								String key = courseId + "_" + materialTargetMonth;

								CourseSalesRefund dto = summaryMap.computeIfAbsent(
										key, k -> new CourseSalesRefund(courseId, materialTargetMonth)
										);
								// 集計結果のリストをDB更新処理へ渡す
								dto.addMaterialFee(summary.getRefundMaterialFee());

			        }
						}
					}


				System.out.println("*****"+refundSummaryMap);
				System.out.println("***** course_sales書き込み *****");
				for (CourseSalesRefund dto : summaryMap.values()) {
				    System.out.printf("講座ID: %s | 対象月: %s | 返金受講料: %,d円 | 返金教材費: %,d円%n",
				        dto.getCourseId(),
				        dto.getTargetMonth(),
				        dto.getRefundTuitionFee(),
				        dto.getRefundMaterialFee()
				    );
				}

			LocalDateTime today = LocalDateTime.now();

			List<Integer> targetIds = schedules.stream()
					.filter(s -> {
						// Date と StartTime を結合して LocalDateTime を作成
						LocalDateTime scheduleDateTime = LocalDateTime.of(s.getDate(), s.getStartTime());
						// 現在日時より前でない（＝現在時刻以降）コマを残す
						boolean isFuture = !scheduleDateTime.isBefore(today);
						// ステータスの判定（すでに休講または中止になっていないか）
						// ※「CANCELLED_SESSION」および「CANCELLED_COURSE」を除外対象にする
						String status = s.getScheduleStatus();
						boolean isNotCancelled = !"CANCELLED_SESSION".equals(status)
																	&& !"CANCELLED_COURSE".equals(status);

						// 両方の条件を満たす（未来の日程 且つ まだ休講/中止されていない）コマだけを残す
						return isFuture && isNotCancelled;

         // return !scheduleDateTime.isBefore(today);
				})
					.map(ClassRoomSchedule::getId)
					.collect(Collectors.toList());

    System.out.println("class_room_schedule書き込みcrsId"+targetIds);//class_room_schedule用

    // 更新対象のIDが存在する場合のみ、一括更新処理を1回だけ呼ぶ
    // //////class_room_schedule書き込み//////
    	if (!targetIds.isEmpty()) {
    			refundMapper.updateCrsStatusForCancellation(targetIds, "CANCELLED_COURSE");
    	}
    //変更前のcourse_salesの取得
			List<CourseSales>beforeList=courseMapper.selectCourseSalesByCourseId(courseId);
			System.out.println("beforeList-refund----"+beforeList);
			Map<String, CourseSales> beforeMap = beforeList.stream()
          .collect(Collectors.toMap(CourseSales::getTargetMonth, cs -> cs));

    //　//////course_sales書き込み////////
    	List<CourseSalesRefund>dtoList=new ArrayList<>(summaryMap.values());
    	System.out.println("*****dtoList"+dtoList);

    	if(!dtoList.isEmpty()) {
    		refundMapper.updateCourseSalesForRefund(dtoList);

    	}
    	//変更後のcourse_salesの取得とMap化
			List<CourseSales>afterList=courseMapper.selectCourseSalesByCourseId(courseId);
			System.out.println("afterList-refund----"+afterList);
			Map<String, CourseSales> afterMap = afterList.stream()
          .collect(Collectors.toMap(CourseSales::getTargetMonth, cs -> cs));

		// 比較対象となる全対象月（targetMonth）の抽出（beforeMapとafterMapのキーの和集合）
			Set<String> allMonths=new HashSet<>();
			allMonths.addAll(beforeMap.keySet());
			allMonths.addAll(afterMap.keySet());

			//beforeMapとafterMapを比較
			for(String month:allMonths) {
				CourseSales before=beforeMap.get(month);
				CourseSales after=afterMap.get(month);

				System.out.println("refund---"+allMonths);
				System.out.println("refund変更前---"+before);
				System.out.println("refund変更後---"+after);

				int beforeTuition=(before !=null)?before.getMonthlyTuitionFee():0;
				int beforeMaterial=(before !=null)?before.getMaterialFee():0;
				int afterTuition=(after !=null)?after.getMonthlyTuitionFee():0;
				int afterMaterial=(after !=null)?after.getMaterialFee():0;

				int beforeRefundTuition=(before !=null)?before.getRefundMonthlyTuitionFee():0;
				int afterRefundTuition=(after !=null)?after.getRefundMonthlyTuitionFee():0;
				int beforeRefundMaterial=(before !=null)?before.getRefundMaterialFee():0;
				int afterRefundMaterial=(after !=null)?after.getRefundMaterialFee():0;

				int diffTuition=afterRefundTuition-beforeRefundTuition;
				int diffMaterial=afterRefundMaterial-beforeRefundMaterial;

				//金額に差分がある月をログに記録
				if(beforeTuition != afterTuition || beforeMaterial != afterMaterial ||
						diffTuition !=0 || diffMaterial !=0) {

					memberService.recordCourseSalesLog
					(courseId,"CANCELLED_COURSE",
							month,beforeTuition,beforeMaterial,afterTuition,afterMaterial,
							diffTuition,diffMaterial);
			}
		}

			return true;
		}

		@Override
		public boolean servCancellSession(String courseId,Integer targetId){

			List<Course>courseList=courseService.servSellectCourseByCourseId2(courseId);
				//講座情報なし判定
			 if (courseList == null || courseList.isEmpty()) {
	        return false;
	    }

			Course course=courseList.get(0);
			List<ClassRoomSchedule> schedules = course.getClassRoomSchedule();
				//スケジュール情報なし判定
			 if (schedules == null || schedules.isEmpty()) {
	        return false;
	    }
		    if (targetId != null && targetId>0) {
	        refundMapper.updateCrsStatusForCancelledSession(targetId, "CANCELLED_SESSION");
	    }


      return true;

		}
		@Override
		public boolean servCancellSessionWithMemberId(String courseId,Integer targetId) {

			List<Course>courseList=courseService.servSellectCourseByCourseId2(courseId);
			//講座情報なし判定
				if (courseList == null || courseList.isEmpty()) {
					return false;
				}

			Course course=courseList.get(0);
			List<ClassRoomSchedule> schedules = course.getClassRoomSchedule();
		//スケジュール情報なし判定
				if (schedules == null || schedules.isEmpty()) {
					return false;
			}
				Map<Integer,RefundSummary> refundSummaryMap =
						calculateRefundSummaryForSession(courseId,targetId);
//				Set<Integer>memberIds=refundSummaryMap.keySet();
				//course_history用
					System.out.println("collection*****course_history書き込み");
					for (Map.Entry<Integer, RefundSummary> entry : refundSummaryMap.entrySet()) {
						Integer memberId = entry.getKey();
						RefundSummary summary = entry.getValue();

						int refundtuitionFee = summary.getRefundTuitionFee();   // 返金受講料
						int refundmaterialFee = summary.getRefundMaterialFee(); // 返金教材費

			    // 個別のDB更新や通知処理を実施
						System.out.println("memberId:"+memberId+" courseId:"+
								courseId+" tuitionFee:"+refundtuitionFee+" materialFee:"+refundmaterialFee);
					// ////////course_history書き込み////////
//					if (memberId != null || courseId.isEmpty()) {
						if (memberId != null && courseId != null && !courseId.isEmpty()) {
							refundMapper.updateCourseHistoryForRefund(memberId,courseId,refundtuitionFee,refundmaterialFee);
							}

					}
					DateTimeFormatter formatter=
							DateTimeFormatter.ofPattern("yyyyMM");//getCrsDate().format(formatter);
				// キー: "courseId_targetMonth" (例: "2026070007_2026-09")
					Map<String, CourseSalesRefund> summaryMap = new HashMap<>();

						for (RefundSummary summary : refundSummaryMap.values()) {
							List<ScheduleAccountingDetail> remainingSchedules =
									summary.getRemainingSchedules();

							// 未受講の対象講座（remainingSchedules）が存在しない場合はスキップ
							if (remainingSchedules == null || remainingSchedules.isEmpty()) {
								continue;
							}
							//受講料の集計（コマごとにループして対象月へ加算）
							for (ScheduleAccountingDetail detail : remainingSchedules) {
									if (detail.getCrsDate() != null) {

										//String courseId = detail.getChCourseId();
										String tuitionTargetMonth = detail.getCrsDate().format(formatter);
										// 複合キーの作成
										String key = courseId + "_" + tuitionTargetMonth;

										CourseSalesRefund dto = summaryMap.computeIfAbsent(
												key, k -> new CourseSalesRefund(courseId, tuitionTargetMonth)
												);
										dto.addTuitionFee(detail.getCTuitionFee());
					        }
								}

//							//教材費の集計（返金対象の教材費がある場合のみ、未受講講座の初回日付へ加算）
//							if(summary.getRefundMaterialFee()>0) {
//							// 未受講講座の先頭コマを取得
//								ScheduleAccountingDetail firstRemainingSchedule = remainingSchedules.get(0);
//								if(firstRemainingSchedule.getCrsDate()!=null) {
//									String materialTargetMonth = firstRemainingSchedule.getCrsDate().format(formatter);
//									String key = courseId + "_" + materialTargetMonth;
//
//									CourseSalesRefund dto = summaryMap.computeIfAbsent(
//											key, k -> new CourseSalesRefund(courseId, materialTargetMonth)
//											);
//									// 集計結果のリストをDB更新処理へ渡す
//									dto.addMaterialFee(summary.getRefundMaterialFee());
//
//				        }
//							}
						}


					System.out.println("collection*****"+refundSummaryMap);
					System.out.println("session*****course_sales書き込み *****");
					for (CourseSalesRefund dto : summaryMap.values()) {
					    System.out.printf("講座ID: %s | 対象月: %s | 返金受講料: %,d円 | 返金教材費: %,d円%n",
					        dto.getCourseId(),
					        dto.getTargetMonth(),
					        dto.getRefundTuitionFee(),
					        dto.getRefundMaterialFee()
					    );
					}


		    System.out.println("session*****class_room_schedule書き込みcrsId"+targetId);//class_room_schedule用

		    // 更新対象のIDが存在する場合のみ、一括更新処理を1回だけ呼ぶ
		    // //////class_room_schedule書き込み//////
		    if (targetId != null && targetId>0) {
	        refundMapper.updateCrsStatusForCancelledSession(targetId, "CANCELLED_SESSION");
		    }

		    //変更前のcourse_salesの取得
					List<CourseSales>beforeList=courseMapper.selectCourseSalesByCourseId(courseId);
					System.out.println("session*****beforeList----"+beforeList);
					Map<String, CourseSales> beforeMap = beforeList.stream()
		          .collect(Collectors.toMap(CourseSales::getTargetMonth, cs -> cs));

		    //　//////course_sales書き込み////////
		    	List<CourseSalesRefund>dtoList=new ArrayList<>(summaryMap.values());
		    	System.out.println("session*****dtoList"+dtoList);

		    	if(!dtoList.isEmpty()) {
		    		refundMapper.updateCourseSalesForRefund(dtoList);
//
		    	}
		    	//変更後のcourse_salesの取得とMap化
//					List<CourseSales>afterList=courseMapper.selectCourseSalesByCourseId(courseId);

//					Map<String, CourseSales> afterMap = afterList.stream()
//		          .collect(Collectors.toMap(CourseSales::getTargetMonth, cs -> cs));
//
//				// 比較対象となる全対象月（targetMonth）の抽出（beforeMapとafterMapのキーの和集合）
//					Set<String> allMonths=new HashSet<>();
//					allMonths.addAll(beforeMap.keySet());
//					allMonths.addAll(afterMap.keySet());

				// /////////////////////////////初回月確認

					List<ScheduleAccountingDetail>sad=
							scheduleStatusMapper.selectScheduleAccountingDetails(courseId);
						if(sad.isEmpty()) {
								throw new IllegalStateException("月別回数データが存在しないため処理を中断しました"+courseId);
						}

						// 処理済み（計算済み）の会員IDを記録する Set
						Set<Integer>processedMemberIds=new HashSet<>();


				// =========================================================
				// 0. 今回の休講コマ（targetId）および過去の休講・中止コマを除外した有効リストを作成
				// =========================================================
				List<ScheduleAccountingDetail> activeSad = sad.stream()
				    .filter(s -> !targetId.equals(s.getCrsId())) // 今回の休講コマを除外
				    .filter(s -> !"CANCELLED_SESSION".equals(s.getCrsScheduleStatus())) // 既休講を除外
				    .filter(s -> !"CANCELLED_COURSE".equals(s.getCrsScheduleStatus()))  // 既中止を除外
				    .collect(Collectors.toList());

				// 1. ループの前に「会員ごとの最も古い受講日」をあらかじめ集計する（activeSadを使用）
				Map<Integer, LocalDate> memberFirstDateMap = new HashMap<>();
				for (ScheduleAccountingDetail s : activeSad) { // ★activeSad に変更
				    Integer memberId = s.getChMemberId();
				    LocalDate crsDate = s.getCrsDate();

				    if (!memberFirstDateMap.containsKey(memberId) ||
				        crsDate.isBefore(memberFirstDateMap.get(memberId))) {
				        memberFirstDateMap.put(memberId, crsDate);
				    }
				}

				// 2. メインの集計ループ（activeSadを使用）
				for (ScheduleAccountingDetail s : activeSad) { // ★activeSad に変更
				    Integer memberId = s.getChMemberId();

				    if (!processedMemberIds.contains(memberId)) {
				        LocalDate memberFirstDate = memberFirstDateMap.get(memberId);

				        // 全コマ休講などで有効コマが0件になった場合のnullチェック（安全対策）
				        if (memberFirstDate != null) {
				            String newStartMonth = memberFirstDate.format(formatter);
				            String currentStartMonth = s.getChStartMonth();// 変更前の月（旧月）を保持
				            // 初回月（startMonth）に変更が発生した場合のみ実行
				            if (!currentStartMonth.equals(newStartMonth)) {
				            		// 1. course_history の startMonth を更新
				                memberMapper.updateCourseHistoryStartMonth(
				                    memberId,courseId,newStartMonth);
				                // 2. 教材費（chPaidMaterialFee）を取得
				                Integer pmFee = s.getChPaidMaterialFee();
				                if (pmFee != null && pmFee > 0) {

				                // 旧月(currentStartMonth)から新月(newStartMonth)へ pmFee 分を付け替え
				                	refundMapper.updateCourseSalesMaterialFee(
				                  		courseId, currentStartMonth, newStartMonth,pmFee);
				              }
				                // メモリ上の値を更新
				                s.setChStartMonth(newStartMonth);
				            }
				        }
				        processedMemberIds.add(memberId);// 処理済み会員として記録
				    }
				}
			// 2. ★すべての DB 更新が完了した後に「変更後の course_sales」を取得する
				List<CourseSales> afterList = courseMapper.selectCourseSalesByCourseId(courseId);
			System.out.println("session*****afterList----"+afterList);
				Map<String, CourseSales> afterMap = afterList.stream()
				    .collect(Collectors.toMap(CourseSales::getTargetMonth, cs -> cs));

				// 3. 全対象月の抽出と比較・ログ記録
				Set<String> allMonths = new HashSet<>();
				allMonths.addAll(beforeMap.keySet());
				allMonths.addAll(afterMap.keySet());

					//beforeMapとafterMapを比較
					for(String month:allMonths) {
						CourseSales before=beforeMap.get(month);
						CourseSales after=afterMap.get(month);

						System.out.println("cancell---"+allMonths);
						System.out.println("cancell変更前---"+before);
						System.out.println("cancell変更後---"+after);

						int beforeTuition=(before !=null)?before.getMonthlyTuitionFee():0;
						int beforeMaterial=(before !=null)?before.getMaterialFee():0;
						int afterTuition=(after !=null)?after.getMonthlyTuitionFee():0;
						int afterMaterial=(after !=null)?after.getMaterialFee():0;

						int beforeRefundTuition=(before !=null)?before.getRefundMonthlyTuitionFee():0;
						int afterRefundTuition=(after !=null)?after.getRefundMonthlyTuitionFee():0;
						int beforeRefundMaterial=(before !=null)?before.getRefundMaterialFee():0;
						int afterRefundMaterial=(after !=null)?after.getRefundMaterialFee():0;

						int diffTuition=afterRefundTuition-beforeRefundTuition;
						int diffMaterial=afterRefundMaterial-beforeRefundMaterial;

						//金額に差分がある月をログに記録
						if(beforeTuition != afterTuition || beforeMaterial != afterMaterial ||
								diffTuition !=0 || diffMaterial !=0) {

							memberService.recordCourseSalesLog
							(courseId,"CANCELLED_SESSION",
									month,beforeTuition,beforeMaterial,afterTuition,afterMaterial,
									diffTuition,diffMaterial);
					}
				}


			return true;
		}


}









