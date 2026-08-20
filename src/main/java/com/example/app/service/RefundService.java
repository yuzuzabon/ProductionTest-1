package com.example.app.service;

import java.util.Map;

import com.example.app.domain.RefundSummary;

public interface RefundService {

	//払い戻し用
		public Map<Integer, RefundSummary> calculateRefundSummary(String courseId) ;
	//講座中止（受講生なし）
		public boolean servCancellCourse(String courseId);
	//講座中止（受講生あり）
		public boolean servCancellCourseWithMemberId(String courseId);	

}
