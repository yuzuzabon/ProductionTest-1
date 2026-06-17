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
	private String courseId;
	private String targetMonth;
	private Integer monthlyTuitionFee;
	private Integer materialFee;
	
	private LocalDateTime registeredAt;
	private LocalDateTime updatedAt;

}
