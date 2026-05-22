package com.example.app.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassRoom {

		private Integer id;
		private String classRoom;
		private LocalDateTime registeredAt;
		private LocalDateTime updatedAt;
	
}
