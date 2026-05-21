package com.example.app.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseCapacity {

		private String courseCode;
		@NotNull
		@Min(1)
		private Integer capacity;
		private Integer NumberOfApplicant;

}
