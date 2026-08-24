package com.example.app.service;

import java.util.Map;

import com.example.app.domain.RefundSummary;

public interface RefundService {

	//講座中止払い戻し
		public Map<Integer, RefundSummary> calculateRefundSummary(String courseId) ;
	//講座休講払い戻し
		public Map<Integer, RefundSummary> calculateRefundSummaryForSession
			(String courseId, Integer targetId);
	//講座払い戻し	
		public Map<String, RefundSummary> calculateRefundSummaryForMember(Integer memberId);	
	//講座中止（受講生なし）
		public boolean servCancellCourse(String courseId);
	//講座中止（受講生あり）
		public boolean servCancellCourseWithMemberId(String courseId);
	//講座休講（受講生なし）
		public boolean servCancellSession(String courseId,Integer scheduleId);
	//講座休講（受講生あり）
		public boolean servCancellSessionWithMemberId(String courseId,Integer targetId);
	//講座払い戻し
		public boolean servRefundCourseWithMemberId(String targetCourseId,Integer memberId) ;
}
