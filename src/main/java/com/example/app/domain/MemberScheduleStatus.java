package com.example.app.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MemberScheduleStatus {
	
	private Integer id;
	private Integer memberId;
	private String courseId;
	private Integer scheduleId;
	private String status;
	
	private LocalDateTime registeredAt;
	private LocalDateTime updatedAt;

}
