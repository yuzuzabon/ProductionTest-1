package com.example.app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.app.domain.CourseSalesLog;
import com.example.app.mapper.CourseSalesLogMapper;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class CourseSalesLogServiceImpl implements CourseSalesLogService{

	private final CourseSalesLogMapper salesLogMapper;

	@Override
	public List<CourseSalesLog> servSelectCourseSalesLogByCriteria(
			String courseId,String reasonType,String targetMonth){
	// パラメータを Map にセット
    Map<String, Object> params = new HashMap<>();
    params.put("courseId", courseId);
    params.put("reasonType", reasonType);
    params.put("targetMonth", targetMonth);

		return salesLogMapper.selectCourseSalesLogByCriteria(params);
	}
	@Override
	public List<String> servSelectDistinctCourseIds() {
    return salesLogMapper.selectDistinctCourseIds();
	}
	@Override
	public List<String> servSelectDistinctReasonTypes() {
    return salesLogMapper.selectDistinctReasonTypes();
	}
	@Override
	public List<String> servSelectDistinctTargetMonths() {
    return salesLogMapper.selectDistinctTargetMonths();
	}

}
