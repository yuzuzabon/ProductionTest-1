package com.example.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.app.service.ClassRoomService;

@Mapper
public interface ClassRoomMapper {

	//全件
	List<ClassRoomService> SelectRoomAll();
	
}
