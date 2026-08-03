package com.example.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemainingCourseData {

	private boolean ale;
	private Integer ct;
	private Integer tuition;
	private Integer material;
	private Integer remainingCount;
	
}
