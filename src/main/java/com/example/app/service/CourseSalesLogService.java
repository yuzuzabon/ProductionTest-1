package com.example.app.service;

import java.util.List;

import com.example.app.domain.CourseSalesLog;

public interface CourseSalesLogService {

	//売り上げマスタ閲覧
	public List<CourseSalesLog> servSelectCourseSalesLogByCriteria(
			String courseId,String reasonType,String targetMonth);
	//売上マスタ検索条件courseId取得
	public List<String> servSelectDistinctCourseIds();
	//売上マスタ検索条件reasonType取得
	public List<String> servSelectDistinctReasonTypes();
	//売上マスタ検索条件targetMonths取得
	public List<String> servSelectDistinctTargetMonths()
;	
}
