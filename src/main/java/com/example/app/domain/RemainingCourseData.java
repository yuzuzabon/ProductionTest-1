package com.example.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemainingCourseData {

	private boolean ale; //途中受講可否 allowLateEnrollment
	private Integer ct;	//講座回数 CourseTerm	
	private Integer tuition;
	private Integer material;
	private Integer remainingCount;
	
}
