package com.example.app.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.RefundSummary;
import com.example.app.domain.ScheduleAccountingDetail;
import com.example.app.mapper.MemberScheduleStatusMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundServiceImpl implements RefundService{

		private final CourseService courseService;
		private final MemberScheduleStatusMapper scheduleStatusMapper;

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
}









