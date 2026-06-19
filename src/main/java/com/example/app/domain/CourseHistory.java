package com.example.app.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseHistory {
//受講履歴マスタ
		private Integer id;
		private Integer memberId;
		private Integer memberStatus;
		private String courseId;
		private Integer paidTuituonFee;
		private Integer paidMaterialFee;
		private Integer refoundsTuituonFee;
		private Integer refoundsMaterialFee;
				
		private LocalDateTime registeredAt;
		private LocalDateTime updatedAt;
}
