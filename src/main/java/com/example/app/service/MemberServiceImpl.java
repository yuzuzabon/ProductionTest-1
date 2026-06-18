package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.Course;
import com.example.app.domain.CourseCapacity;
import com.example.app.domain.CourseSales;
import com.example.app.domain.Member;
import com.example.app.domain.MonthlyCount;
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
		//@Override
		//public List<ClassRoomSchedule> servSelectClassRoomSchedule(String courseId){
		//		return memberMapper.selectClassRoomSchedule(courseId);
		//}
		
		@Override
		public Course selectCourseFee(String courseId) {
			// TODO 自動生成されたメソッド・スタブ
			return memberMapper.selectCourseFee(courseId);
		}
		@Override
		public List<MonthlyCount> selectMonthlyCount(String courseId) {
			// TODO 自動生成されたメソッド・スタブ
			return memberMapper.selectMonthlyCount(courseId);
		}
		
		@Override
		@Transactional
		public boolean servApplyCourse(Integer id,String courseId) {
			CourseCapacity ca=memberMapper.selectByCourseIdForUpdate(courseId)
						.orElseThrow(() -> new IllegalArgumentException("指定された講座はみつかりません"+courseId));
						
				if(ca.getCapacity()<=ca.getNumberOfApplicant()) {
						return false;
				}
				List<MonthlyCount>mc=memberMapper.selectMonthlyCount(courseId);
				Course cf=memberMapper.selectCourseFee(courseId);
				
				int tuition=cf.getTuitionFee();
				int material=cf.getMaterialFee();
				int term=cf.getCourseTerm();
				
				String initialMonth=mc.get(0).getSalesMonth();
				
				for(MonthlyCount m : mc) {
					CourseSales sales=new CourseSales();
					String targetMonth=m.getSalesMonth();
					Integer monthlyTuitionFee=tuition*m.getCount();
					sales.setCourseId(courseId);
					sales.setTargetMonth(targetMonth);
					sales.setMonthlyTuitionFee(monthlyTuitionFee);
					
					if(targetMonth.equals(initialMonth)) {
						sales.setMaterialFee(material);
					}else {
						sales.setMaterialFee(0);
					}
					System.out.println(sales);
				memberMapper.upsertCourseSales(sales);
				}
				int numberOfApplicant=ca.getNumberOfApplicant()+1;
				memberMapper.updateApplyedCount(courseId,numberOfApplicant);
				
						return true;
				
		}
		
}
