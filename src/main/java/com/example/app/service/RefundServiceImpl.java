package com.example.app.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.ScheduleAccountingDetail;
import com.example.app.mapper.MemberScheduleStatusMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService{
	
		private final CourseService courseService; 
		private final MemberScheduleStatusMapper scheduleStatusMapper;
	
		@Transactional
		public Map<Integer,Map<String,Object>> getRefundSummaryMap(String courseId) {
		//データ取得
			List<ScheduleAccountingDetail> rawList = 
				scheduleStatusMapper.selectScheduleAccountingDetails(courseId);

			return calculateRefundSummary(rawList); // 共通計算ロジックの呼び出し;
		
	}
	
		private Map<Integer, Map<String, Object>>calculateRefundSummary
		(List<ScheduleAccountingDetail> rawList){
		// 今日の日付（開始前/開始後の判定用）
			LocalDateTime today = LocalDateTime.now();
		//chMemberId ごとにグループ化
			Map<Integer, List<ScheduleAccountingDetail>> groupedCourses = rawList.stream()
				.collect(Collectors.groupingBy(
				ScheduleAccountingDetail::getChMemberId,
						LinkedHashMap::new, // 順序を維持
						Collectors.toList()
						));
			
			
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
		
				return refundSummaryMap;
			
		}
	
	
	
	
	
}
