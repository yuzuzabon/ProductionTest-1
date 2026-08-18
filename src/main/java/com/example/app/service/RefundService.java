package com.example.app.service;

import java.util.Map;

import com.example.app.domain.RefundSummary;

public interface RefundService {

	//払い戻し用
		public Map<Integer, RefundSummary> calculateRefundSummary(String courseId) ;

}
