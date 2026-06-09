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
		private Integer members_id;
		private String course_id;
		
		private LocalDateTime registeredAt;
		private LocalDateTime updatedAt;
}
