package com.example.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseCapacity {

		private Integer courseCode;
		private Integer capacity;
		private Integer NumberOfApplicant;
	
}
