package com.example.app.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.app.domain.CourseSalesLog;

@Mapper
public interface CourseSalesLogMapper {
	
	void insertLog(CourseSalesLog log);

}
