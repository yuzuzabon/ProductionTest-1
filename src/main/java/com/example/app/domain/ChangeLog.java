package com.example.app.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeLog {
	
	private Integer id;
	private String courseId;
	private String reasonType;
	private String targetMonth;
	private Integer beforeTuitionFee;
	private Integer beforeMaterialFee;
	private Integer afterTuitionFee;
	private Integer afterMaterialFee;
	private Integer refundsMonthlyTuitionFee=0;
	private Integer refundsMaterialFee=0;
	
	private LocalDateTime registeredAt;
	//private LocalDateTime updatedAt; insertのみの運用
	
}
