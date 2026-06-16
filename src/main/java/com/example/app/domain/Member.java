package com.example.app.domain;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class Member {

		private Integer id;
		@NotBlank
		@Size(max=10)
		private String name;
		@Min(value=0)
		@Max(value =100)
		private Integer age;
		private String address;
		private Integer typeId;
		private LocalDateTime created;
		
}
