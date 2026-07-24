package com.example.app.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.app.domain.ChangeLog;

@Mapper
public interface ChangeLogMapper {
	
	void insertLog(ChangeLog log);

}
