package com.example.app.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseSales {
//売上マスタ
	private Integer id;
	private Integer courseSalesStatus;//現在未使用
	private String courseId;
	private String targetMonth;
	private Integer monthlyTuitionFee;
	private Integer materialFee;
	private Integer refundMonthlyTuitionFee;
	private Integer refundMaterialFee;
	
	private LocalDateTime registeredAt;
	private LocalDateTime updatedAt;

}
