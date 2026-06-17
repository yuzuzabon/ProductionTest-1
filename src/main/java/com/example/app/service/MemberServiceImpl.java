package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.CourseCapacity;
import com.example.app.domain.Member;
import com.example.app.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl  implements MemberService{
		private final MemberMapper memberMapper;
		
		@Override
		public List<Member>servSelectMemberAll(){
				return memberMapper.selectMemberAll();
		}
		@Override
		public List<Member>servSelectMemberByWord(Integer id,String name){
				return memberMapper.selectMemberByWord(id,name);
		}
		@Override
		public Member servSelectMemberById(Integer id) {
				return memberMapper.selectMemberById(id);
		}
		@Override
		@Transactional
		public boolean servApplyCourse(Integer id,String courseId) {
			CourseCapacity ca=memberMapper.selectByCourseIdForUpdate(courseId)
						.orElseThrow(() -> new IllegalArgumentException("指定された講座はみつかりません"+courseId));
						
				if(ca.getCapacity()<=ca.getNumberOfApplicant()) {
						return false;
				}
				int na=ca.getNumberOfApplicant()+1;
				memberMapper.updateApplyedCount(courseId,na);
				
						return true;
				
		}
		
}
