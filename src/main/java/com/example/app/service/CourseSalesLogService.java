package com.example.app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.app.domain.CourseSalesLog;
import com.example.app.mapper.CourseSalesLogMapper;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class CourseSalesLogService {

	@Autowired
	private final CourseSalesLogMapper salesLogMapper;

	public List<CourseSalesLog> servSelectCourseSalesLogByCriteria(
			String courseId,String reasonType,String targetMonth){
	// パラメータを Map にセット
    Map<String, Object> params = new HashMap<>();
    params.put("courseId", courseId);
    params.put("reasonType", reasonType);
    params.put("targetMonth", targetMonth);

		return salesLogMapper.selectCourseSalesLogByCriteria(params);
	}
	public List<String> getDistinctCourseIds() {
    return salesLogMapper.selectDistinctCourseIds();
	}
	public List<String> getDistinctReasonTypes() {
    return salesLogMapper.selectDistinctReasonTypes();
	}
	public List<String> getDistinctTargetMonths() {
    return salesLogMapper.selectDistinctTargetMonths();
	}

}
