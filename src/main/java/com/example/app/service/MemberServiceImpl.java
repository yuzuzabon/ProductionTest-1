package com.example.app.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.ChangeLog;
import com.example.app.domain.Course;
import com.example.app.domain.CourseCapacity;
import com.example.app.domain.CourseHistory;
import com.example.app.domain.CourseSales;
import com.example.app.domain.Member;
import com.example.app.domain.MonthlyCount;
import com.example.app.mapper.ChangeLogMapper;
import com.example.app.mapper.CourseMapper;
import com.example.app.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl  implements MemberService{

		private final MemberMapper memberMapper;
		private final CourseMapper courseMapper;
		private final ChangeLogMapper changeLogMapper;

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
//		@Override
//		public List<CourseSales> selectCourseSalesByCourseId(String courseId) {
//		
//			return courseMapper.selectCourseSalesByCourseId(courseId); 
//		}
		
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
					history.setPaidTuitionFee(term*tuition);
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
					System.out.println(sales);
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
		// 日程変更による受講マスタの更新//////////////////////
		@Override
		@Transactional
		public void servScheduleChangeCourse(String courseId) {
			
			// 講座定員情報の取得 t=申込者数
			CourseCapacity ca=memberMapper.selectByCourseIdForUpdate(courseId)
					.orElseThrow(() -> new IllegalArgumentException("指定された講座が存在しないため処理を中断しました"+courseId));
			Integer t=ca.getNumberOfApplicant();
			// System.out.println("---申込者数="+t);
			// 申込者0のときはcourse_salesへの書き込みをスキップする
			if(t==null || t==0) {
				return;
			}
			// 年月見出し＋月ごとの開催回数を取得
				List<MonthlyCount>mc=memberMapper.selectMonthlyCount(courseId);
				if(mc.isEmpty()) {
						throw new IllegalStateException("月別回数データが存在しないため処理を中断しました"+courseId);
				}
				Course cf=memberMapper.selectCourseFee(courseId);

					int tuition=cf.getTuitionFee();
					int material=cf.getMaterialFee();
					
					String initialMonth=mc.get(0).getSalesMonth();
			    if(initialMonth.isEmpty()) {
			    	throw new IllegalStateException("初回月データが存在しないため処理を中断しました");
			    }
			  //変更前のcourse_salesの取得
				List<CourseSales>beforeList=courseMapper.selectCourseSalesByCourseId(courseId);
				System.out.println("変更前受講料マスタ----"+beforeList); 
//				Map<String,CourseSales>beforeMap=	beforeList.stream()
//						.collect(Collectors.toMap(CourseSales::getTargetMonth,s->s));
				Map<String, CourseSales> beforeMap = new HashMap<>();
				for (CourseSales s : beforeList) {
				    beforeMap.put(s.getTargetMonth(), s);
				}
				//変更前の対象月をbeforeMapのkeyから抽出
				Set<String>allMonths=new HashSet<>(beforeMap.keySet());
				for(MonthlyCount m:mc){
					allMonths.add(m.getSalesMonth());
				}
				System.out.println("変更前受講料マスタmap----"+beforeMap); 
				System.out.println("変更前受講月----"+allMonths); 
			    
			  //受講料と教材費のリセット
			  courseMapper.updateCourseSalesReset(courseId);	
			  
			  //スケジュール変更後の月ごとの受講料と教材費を生成
			  Map<String, CourseSales> afterMap = new HashMap<>();
			  
				for(MonthlyCount m : mc) {
					CourseSales sales=new CourseSales();
					String targetMonth=m.getSalesMonth();

					sales.setCourseId(courseId);
					sales.setTargetMonth(targetMonth);
					sales.setMonthlyTuitionFee(tuition*t*m.getCount());
					sales.setMaterialFee
					(targetMonth.equals(initialMonth) ? material*t : 0);
					System.out.println(sales);
/*				if(targetMonth.equals(initialMonth)) {
						sales.setMaterialFee(material);
					}else {
						sales.setMaterialFee(0);
					}
*/
				//新しいスケジュールで受講料と教材費の書き込み	
				memberMapper.upsertCourseSales(sales);
				afterMap.put(targetMonth, sales);
				}
				//beforeMapとafterMapを比較
				for(String month:allMonths) {
					CourseSales before=beforeMap.get(month);
					CourseSales after=afterMap.get(month);
					
					System.out.println("変更前---"+before);
					System.out.println("変更後---"+after);
					
					int beforeTuition=(before !=null)?before.getMonthlyTuitionFee():0;
					int beforeMaterial=(before !=null)?before.getMaterialFee():0;
					int afterTuition=(after !=null)?after.getMonthlyTuitionFee():0;
					int afterMaterial=(after !=null)?after.getMaterialFee():0;
					
					//金額に差分がある月をログに記録
					if(beforeTuition != afterTuition || beforeMaterial != afterMaterial) {
						ChangeLog log=new ChangeLog();
						log.setCourseId(courseId);
						log.setReasonType("SHEDULE_CHANGE");
						log.setTargetMonth(month);
						log.setBeforeTuitionFee(beforeTuition);
						log.setBeforeMaterialFee(beforeMaterial);
						log.setAfterTuitionFee(afterTuition);
						log.setAfterMaterialFee(afterMaterial);
						System.out.println("log-----"+log);
						changeLogMapper.insertLog(log);
					}
				}
				
		}
		
		//　/////////////////////////////////
		
}
