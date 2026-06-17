package com.example.app.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandBy_CourseFee {
//受講料マスタ　<-course.javaに設定して単独で使わない
		private Integer id;
		private String course_id;
		private Integer tuitionFee;
		private Integer materialFee;
		
		private LocalDateTime registeredAt;
		private LocalDateTime updatedAt;
}
