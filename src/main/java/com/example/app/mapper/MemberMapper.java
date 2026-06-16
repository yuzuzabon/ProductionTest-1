package com.example.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.app.domain.Member;

@Mapper
public interface MemberMapper {
	//全件
	List<Member>selectMemberAll();
	//idまたは名前検索	
	List<Member>selectMemberByWord(
				@Param("id")Integer id,
				@Param("name")String name);
	//id
	public Member selectMemberById(
				@Param("id") Integer id);
}
