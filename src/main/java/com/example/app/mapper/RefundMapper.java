package com.example.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface RefundMapper {
	
		void updateStatusByIds(
				@Param("targetIds") List<Integer> targetIds,
				@Param("status") String status
				);

}
