package com.example.app.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {

		private Integer id;
		private String courseId;

		@NotBlank
		private String title;

		@NotBlank
		private String detail;

		private Integer classRoomId;
		@NotNull
		private Integer numberOfDays;
		@Min(1)

		private LocalDateTime registeredAt;
		private LocalDateTime updatedAt;
		@Valid
		private CourseCapacity courseCapacity;
		@Valid
		private ClassRoom classRoom;
		@Valid
		private List<ClassRoomSchedule> classRoomSchedule;
}
