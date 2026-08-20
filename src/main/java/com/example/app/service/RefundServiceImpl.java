package com.example.app.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.domain.CourseSalesRefund;
import com.example.app.domain.RefundSummary;
import com.example.app.domain.ScheduleAccountingDetail;
import com.example.app.mapper.MemberScheduleStatusMapper;
import com.example.app.mapper.RefundMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundServiceImpl implements RefundService{

		private final CourseService courseService;
		private final MemberScheduleStatusMapper scheduleStatusMapper;
		private final RefundMapper refundMapper;

		@Override
		public Map<Integer, RefundSummary> calculateRefundSummary(String courseId) {
		//get用：画面表示用の集計データ取得
			List<ScheduleAccountingDetail> rawList =
				scheduleStatusMapper.selectScheduleAccountingDetails(courseId);

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
            // Date と StartTime を結合して LocalDateTime を作成
            LocalDateTime scheduleDateTime = LocalDateTime.of(s.getDate(), s.getStartTime());
            // 現在日時より前でない（＝現在時刻以降）コマを残す
            return !scheduleDateTime.isBefore(today);
        })
          .map(ClassRoomSchedule::getId)
          .collect(Collectors.toList());

      System.out.println("******PUT側"+targetIds);
	    
	    // 更新対象のIDが存在する場合のみ、一括更新処理を1回だけ呼ぶ
//	    if (!targetIds.isEmpty()) {
//	        refundMapper.updateStatusByIds(targetIds, "CANCELLED_COURSE");
//	    }

	    
			return true;
		}
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

					int tuitionFee = summary.getRefundTuitionFee();   // 返金受講料
					int materialFee = summary.getRefundMaterialFee(); // 返金教材費

		    // 個別のDB更新や通知処理を実施
					System.out.println("memberId:"+memberId+" courseId:"+courseId+" tuitionFee:"+tuitionFee+" materialFee:"+materialFee);
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
				System.out.println("=== 集計結果表示 ===");
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
          return !scheduleDateTime.isBefore(today);
				})
					.map(ClassRoomSchedule::getId)
					.collect(Collectors.toList());
			
    System.out.println("class_room_schedule書き込みcrsId"+targetIds);//class_room_schedule用

    // 更新対象のIDが存在する場合のみ、一括更新処理を1回だけ呼ぶ
//  if (!targetIds.isEmpty()) {
//      refundMapper.updateStatusByIds(targetIds, "CANCELLED_COURSE");
//  }
			
			return false;
		}
		
}









