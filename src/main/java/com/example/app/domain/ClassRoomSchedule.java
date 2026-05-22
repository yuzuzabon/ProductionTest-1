package com.example.app.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassRoomSchedule {
	
	private Integer id;
	private String courseId;
	private Integer classRoomId;
	private LocalDate Date;
	private LocalTime startTime;
	private LocalTime endTime;
	
	private LocalDateTime registeredAt;
	private LocalDateTime updatedAt;
		
}
