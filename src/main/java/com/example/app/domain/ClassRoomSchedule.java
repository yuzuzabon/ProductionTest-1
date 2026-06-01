package com.example.app.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.validation.Valid;

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
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	private Integer coursePeriod;

	private LocalDateTime registeredAt;
	private LocalDateTime updatedAt;

	@Valid
	private ClassRoom classRoom;

}
