package com.example.app.service;

import java.util.List;

import com.example.app.domain.Course;
import com.example.app.domain.Member;
import com.example.app.domain.MonthlyCount;

public interface MemberService {

	//全件
		List<Member>servSelectMemberAll();
	//idまたは名前検索	
		List<Member>servSelectMemberByWord(Integer id,String name);
	//id
		public Member servSelectMemberById(Integer id);
	//講座申し込み　
		public boolean servApplyCourse(Integer id,String courseId);
	//講座申し込み時の受講料振り分け用
		public List<MonthlyCount> selectMonthlyCount(String courseId);
		public Course selectCourseFee(String courseId);
	//	
}
