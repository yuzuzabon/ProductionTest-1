package com.example.app.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundSummary {

		private List<ScheduleAccountingDetail> details;
		private ScheduleAccountingDetail firstItem;
		private long totalCount;
		private long remainingCount;
		private List<ScheduleAccountingDetail> remainingSchedules;
		private boolean isBeforeStart;
		private boolean isFinished;
		private int refundTuitionFee;
		private int refundMaterialFee;
		private int totalRefundAmount;
}
