package com.example.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.Course;
import com.example.app.domain.CourseCapacity;
import com.example.app.domain.CourseHistory;
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
		public List<CourseHistory>servSellectCourseHistoryById(Integer id){
			return memberMapper.sellectCourseHistoryById(id);
		}
		
		@Override
		@Transactional
		public boolean servApplyCourse(Integer id,String courseId) {
			// 講座定員情報の取得
			CourseCapacity ca=memberMapper.selectByCourseIdForUpdate(courseId)
						.orElseThrow(() -> new IllegalArgumentException("指定された講座が存在しないため処理を中断しました"+courseId));
			// 満員をチェック			
				if(ca.getCapacity()<=ca.getNumberOfApplicant()) {
						return false;//　満員
				}
			// 年月見出し＋月ごとの開催回数を取得	
				List<MonthlyCount>mc=memberMapper.selectMonthlyCount(courseId);
				if(mc.isEmpty()) {
						throw new IllegalStateException("月別回数データが存在しないため処理を中断しました"+courseId);
				}
				Course cf=memberMapper.selectCourseFee(courseId);
				
					int tuition=cf.getTuitionFee();
					int material=cf.getMaterialFee();
					int term=cf.getCourseTerm();
				
						
					CourseHistory history=new CourseHistory();
					
					history.setCourseId(courseId);
					history.setMemberId(id);
					history.setMemberStatus(1);
					history.setPaidTuituonFee(term*tuition);
					history.setPaidMaterialFee(material);
// /////////// test環境では重複チェックをコメントアウト //////////					
/*					int overlapCount = 
							memberMapper.countOverlappedCourse(history);
*/		    // 重複がある（カウントが1以上）場合は、更新せずにfalseを返して処理を中断
					int overlapCount = 0;//test環境でのダミーフラグ 
			    if (overlapCount > 0) {
			    	System.out.println(overlapCount);
			    	
			        return false;// 重複
			    }	else {
					
				memberMapper.insertCourseHistory(history);
					System.out.println(history);}
				
			    String initialMonth=mc.get(0).getSalesMonth();
			    if(initialMonth.isEmpty()) {
			    	throw new IllegalStateException("初回月データが存在しないため処理を中断しました");
			    }
				for(MonthlyCount m : mc) {
					CourseSales sales=new CourseSales();
					String targetMonth=m.getSalesMonth();
					
					sales.setCourseId(courseId);
					sales.setTargetMonth(targetMonth);
					sales.setMonthlyTuitionFee(tuition*m.getCount());
					sales.setMaterialFee
					(targetMonth.equals(initialMonth) ? material : 0);
					
/*				if(targetMonth.equals(initialMonth)) {
						sales.setMaterialFee(material);
					}else {
						sales.setMaterialFee(0);
					}
*/					
				memberMapper.upsertCourseSales(sales);
				
				}
			// 申込者数の更新
			// 	numberOfApplicant=numberOfApplicant+1をSQL側で処理
			//int numberOfApplicant=ca.getNumberOfApplicant()+1;
				memberMapper.updateApplyedCount(courseId);
				
						return true;
				
		}
		
}
