package com.example.app.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.app.domain.CourseSalesLog;

@Mapper
public interface CourseSalesLogMapper {
	//course_sales_log書き込み
	void insertLog(CourseSalesLog log);
//course_sales_log全件表示
//public List<CourseSalesLog> selectCourseSalesLogAll();
//course_sales_log検索条件表示
	public List<CourseSalesLog> selectCourseSalesLogByCriteria(Map<String, Object> params);
	List<String> selectDistinctCourseIds();
	List<String> selectDistinctReasonTypes();
	List<String> selectDistinctTargetMonths();
}
