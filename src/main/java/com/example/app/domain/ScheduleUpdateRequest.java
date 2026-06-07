package com.example.app.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleUpdateRequest {
	@NotNull
	private Integer id;
	private String courseId;
	private Integer classRoomId;
	private LocalDate date;
	private LocalTime startTime;
	private Integer coursePeriod;
}
