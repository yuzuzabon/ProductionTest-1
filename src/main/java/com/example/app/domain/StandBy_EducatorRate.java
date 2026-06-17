package com.example.app.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandBy_EducatorRate {
//講師料単価マスタ
		private Integer id;
		private Integer membersId;
		private Integer fixedFee;
		private Integer CommissionBasedFee;
	
	
		private LocalDateTime registeredAt;
		private LocalDateTime updatedAt;
}
