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
	//-- RESERVED(受講予定), ATTENDED(受講済), REFUNDED(返金済), CANCELLED(キャンセル)
	
	private LocalDateTime registeredAt;
	private LocalDateTime updatedAt;

}
