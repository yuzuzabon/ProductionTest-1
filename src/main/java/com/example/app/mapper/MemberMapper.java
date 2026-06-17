package com.example.app.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.app.domain.CourseCapacity;
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
	//受講申し込み
	Optional<CourseCapacity>selectByCourseIdForUpdate(
				@Param("courseId") String courseId);
	//申込後の申込者数加算
	int updateApplyedCount(
				@Param("courseId")String courseId, int na);
}
