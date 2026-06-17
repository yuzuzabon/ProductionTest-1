package com.example.app.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseWithClassRoom {

		private Integer id;
		private String courseId;

		@NotBlank
		private String title;

		@NotBlank
		@Size(max=2000)
		private String detail;

		private Integer classRoomId;
		@NotNull
		@Min(1)
		//private Integer numberOfDays;
		private Integer courseTerm;
		
		@NotNull
		@Min(0)
		private Integer tuitionFee;
		
		@NotNull
		@Min(0)
		private Integer materialFee;
		
		private LocalDateTime registeredAt;
		private LocalDateTime updatedAt;
		@Valid
		private CourseCapacity courseCapacity;
		//@Valid
		//private ClassRoom classRoom;
		@Valid
		private List<ClassRoomSchedule> classRoomSchedule;
}
