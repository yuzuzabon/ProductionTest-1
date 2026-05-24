package com.example.app.domain;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseCapacity {

		private String courseId;
		@NotNull
		@Min(1)
		private Integer capacity;//定員
		private Integer numberOfApplicant;//申込者数
		private LocalDateTime registeredAt;
		private LocalDateTime updatedAt;

}
