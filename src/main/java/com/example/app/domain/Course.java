package com.example.app.domain;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class Course {

		private Integer id;
		
		@NotBlank
		private String title;
		@NotBlank
		private String place;
		@NotNull
		@Min(value=0)
		@Max(value=5)
		private Integer capacity;
		private Integer numberOfApplicant;
		private LocalDateTime created;
	
}
