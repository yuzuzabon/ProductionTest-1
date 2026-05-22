package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.app.mapper.ClassRoomMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class ClassRoomServiceImpl implements ClassRoomService{
	
		private final ClassRoomMapper classRoomMapper;

		@Override
		public List<ClassRoomService> servSelectRoomAll() {
			// TODO 自動生成されたメソッド・スタブ
			return classRoomMapper.SelectRoomAll();
		}

}
