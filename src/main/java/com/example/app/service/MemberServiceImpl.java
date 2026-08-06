package com.example.app.service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.ClassRoomSchedule;
import com.example.app.domain.Course;
import com.example.app.domain.CourseCapacity;
import com.example.app.domain.CourseHistory;
import com.example.app.domain.CourseSales;
import com.example.app.domain.CourseSalesLog;
import com.example.app.domain.Member;
import com.example.app.domain.MemberScheduleStatus;
import com.example.app.domain.MonthlyCount;
import com.example.app.domain.RemainingCourseData;
import com.example.app.domain.ScheduleAccountingDetail;
import com.example.app.mapper.CourseMapper;
import com.example.app.mapper.CourseSalesLogMapper;
import com.example.app.mapper.MemberMapper;
import com.example.app.mapper.MemberScheduleStatusMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl  implements MemberService{

		private final MemberMapper memberMapper;
		private final CourseMapper courseMapper;
		private final CourseService courseService;
		private final CourseSalesLogMapper courseSalesLogMapper;
		private final MemberScheduleStatusMapper scheduleStatusMapper;

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
		//ログ記録用共通部分
		private void recordCourseSalesLog
		(String courseId,String reasonType,String targetMonth,
				Integer beforeTuition,Integer beforeMaterial,
				Integer afterTuition,Integer afterMaterial) {
			CourseSalesLog log=new CourseSalesLog();
			log.setCourseId(courseId);
			log.setReasonType(reasonType);
			log.setTargetMonth(targetMonth);
			log.setBeforeTuitionFee(beforeTuition);
			log.setBeforeMaterialFee(beforeMaterial);
			log.setAfterTuitionFee(afterTuition);
			log.setAfterMaterialFee(afterMaterial);
			
// test---
			courseSalesLogMapper.insertLog(log);
			System.out.println("courseSalelog---"+log);
		}
		//全回数受講(true)途中受講判定(false)/////////////////
		@Override
		public boolean servisFullCourseEnrollment(String courseId){
			RemainingCourseData rcData=
				servSelectRemainingCourseData(courseId);
			//boolean ale=rcData.isAle();
			int ct=rcData.getCt();
			int rc=rcData.getRemainingCount();
			//途中受講の判定
			//途中受講可allow_late_enrollment=1　
			//講座回数 course_term > 残り回数 remainingCount
			
			return ct==rc;
		}
		//途中受講の可否判定
		@Override
		public boolean servisValidLateEnrollment(String courseId) {
			RemainingCourseData rcData = servSelectRemainingCourseData(courseId);
	    // 途中受講許可があり、かつ残回数が1以上あるか
	    return rcData.isAle() && rcData.getRemainingCount() > 0;
		}
		
		
		
		//新規受講
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
			//講座スケジュール情報取得
				List<ClassRoomSchedule>crs=
						memberMapper.sellectCourseSchedulesByCourseId(courseId);
				if(crs.isEmpty()) {
					throw new IllegalStateException("講座スケジュールデータが存在しないため処理を中断しました"+courseId);
				}
			//初回月を抽出		
				String initialMonth=mc.get(0).getSalesMonth();
		    if(initialMonth.isEmpty()) {
		    	throw new IllegalStateException("初回月データが存在しないため処理を中断しました");
		    }
				
				Course cf=memberMapper.selectCourseFee(courseId);

					int tuition=cf.getTuitionFee();
					int material=cf.getMaterialFee();
					int courseTerm=cf.getCourseTerm();

					CourseHistory history=new CourseHistory();

					history.setCourseId(courseId);
					history.setMemberId(id);
					history.setMemberStatus(1);
					history.setLateEnrollment(false);
					history.setCourseTerm(courseTerm);
					history.setRemainingCount(courseTerm);
					history.setPaidTuitionFee(courseTerm*tuition);
					history.setPaidMaterialFee(material);
					history.setStartMonth(initialMonth);
// /////////// test環境では重複チェックをコメントアウト //////////
/**/					int overlapCount =
							memberMapper.countOverlappedCourse(history);
/**/		    // 重複がある（カウントが1以上）場合は、更新せずにfalseを返して処理を中断
// test---					int overlapCount = 0;//test環境でのダミーフラグ
			    if (overlapCount > 0) {
			    	System.out.println(overlapCount);

			        return false;// 重複
			    }	else {

// test---
			    	memberMapper.insertCourseHistory(history);
					System.out.println("新規history---"+history);
			    System.out.println("新規mc---"+mc);
			    System.out.println("新規crs---"+crs);}

			   
				for(MonthlyCount m : mc) {
					CourseSales sales=new CourseSales();
					String targetMonth=m.getSalesMonth();
					Integer calcTuition=tuition*m.getCount();
					Integer calcMaterial=targetMonth.equals(initialMonth) ? material : 0;

					//売り上げマスタへの記録
					sales.setCourseId(courseId);
					sales.setTargetMonth(m.getSalesMonth());
					sales.setMonthlyTuitionFee(tuition*m.getCount());
					sales.setMaterialFee(targetMonth.equals(initialMonth) ? material : 0);
					//System.out.println(sales);
/*				if(targetMonth.equals(initialMonth)) {
						sales.setMaterialFee(material);
					}else {
						sales.setMaterialFee(0);
					}
*/
// test---
					memberMapper.upsertCourseSales(sales);
					System.out.println("新規sales---"+sales);
				//申し込みログ記録
//				CourseSalesLog log=new CourseSalesLog();
//				log.setCourseId(courseId);
//				log.setReasonType("APPLICATION");
//				log.setTargetMonth(targetMonth);
//				log.setBeforeTuitionFee(0);
//				log.setBeforeMaterialFee(0);
//				log.setAfterTuitionFee(calcTuition);
//				log.setAfterMaterialFee(calcMaterial);
//				//System.out.println("application log-----"+log);
				//courseSalesLogMapper.insertLog(log);
				recordCourseSalesLog
				(courseId,"APPLICATION",targetMonth,0,0,calcTuition,calcMaterial);
				
				}
				//
				for(ClassRoomSchedule c : crs) {
					MemberScheduleStatus scheduleStatus
					=new MemberScheduleStatus();
					scheduleStatus.setMemberId(history.getMemberId());
					scheduleStatus.setCourseId(courseId);
					scheduleStatus.setScheduleId(c.getId());
					scheduleStatus.setStatus("RESERVED");
					
// test---
					memberMapper.upsertMemberScheduleStatus(scheduleStatus);
					System.out.println("新規受講status"+scheduleStatus);
				}
				
			// 申込者数の更新
			// 	numberOfApplicant=numberOfApplicant+1をSQL側で処理
			//int numberOfApplicant=ca.getNumberOfApplicant()+1;
// test---
				memberMapper.updateApplyedCount(courseId);

						return true;

		}
		//途中受講時の受講料教材費残回数取得用/////////////////////////////////
		@Override
		
		public RemainingCourseData servSelectRemainingCourseData(String courseId) {

				//途中受講用の情報取得
				Course cf=memberMapper.selectCourseFee(courseId);
					boolean ale=cf.getAllowLateEnrollment();
					Integer ct=cf.getCourseTerm();
					Integer tuition=cf.getTuitionFee();
					Integer material=cf.getMaterialFee();
					
					// 残数取得
					Integer remainingCount=memberMapper.selectRemainingCount(courseId);
					
					return new RemainingCourseData(ale,ct,tuition,material,remainingCount);
		}

		//途中受講　申し込み/////////////////////////////////
		@Override
		@Transactional
		public boolean servRemainingApplyCourse(Integer id,String courseId) {
			// 講座定員情報の取得
			CourseCapacity ca=memberMapper.selectByCourseIdForUpdate(courseId)
						.orElseThrow(() -> new IllegalArgumentException("指定された講座が存在しないため処理を中断しました"+courseId));
			// 満員をチェック
				if(ca.getCapacity()<=ca.getNumberOfApplicant()) {
						return false;//　満員
				}
			// 年月見出し＋月ごとの開催回数を取得
				List<MonthlyCount>mc=memberMapper.selectRemainingMonthlyCount(courseId);
				if(mc.isEmpty()) {
						throw new IllegalStateException("月別回数データが存在しないため処理を中断しました"+courseId);
				}
			//講座スケジュール情報取得
				List<ClassRoomSchedule>crs=
						memberMapper.sellectCourseSchedulesByCourseId(courseId);
				if(crs.isEmpty()) {
					throw new IllegalStateException("講座スケジュールデータが存在しないため処理を中断しました"+courseId);
				}
			//初回月を抽出	
		    String initialMonth=mc.get(0).getSalesMonth();
		    if(initialMonth.isEmpty()) {
		    	throw new IllegalStateException("初回月データが存在しないため処理を中断しました");
		    }
				
				Course cf=memberMapper.selectCourseFee(courseId);

					int tuition=cf.getTuitionFee();
					int material=cf.getMaterialFee();
					int courseTerm=cf.getCourseTerm();
					int remainingCount=mc.stream()
							.mapToInt(MonthlyCount::getCount)
							.sum();
//				int term=memberMapper.selectRemainingCount(courseId);
//				int term=0;
//				for(MonthlyCount m:mc) {
//				term +=m.getCount();
//				}

				//受講生マスタへの記録
				CourseHistory history=new CourseHistory();

				history.setCourseId(courseId);
				history.setMemberId(id);
				history.setMemberStatus(1);
				history.setLateEnrollment(true);
				history.setCourseTerm(courseTerm);
				history.setRemainingCount(remainingCount);
				history.setPaidTuitionFee(remainingCount*tuition);
				history.setPaidMaterialFee(material);
				history.setStartMonth(initialMonth);
					
// test---
				memberMapper.insertCourseHistory(history);
				System.out.println("途中受講history---"+history);
		    System.out.println("途中mc---"+mc);
		    System.out.println("途中crs---"+crs);
			
// /////////// test環境では重複チェックをコメントアウト //////////
/**/					int overlapCount =
							memberMapper.countOverlappedCourse(history);
/**/		    // 重複がある（カウントが1以上）場合は、更新せずにfalseを返して処理を中断
// test---					int overlapCount = 0;//test環境でのダミーフラグ
			    if (overlapCount > 0) {
			    	System.out.println(overlapCount);

			        return false;// 重複
			    }	

				for(MonthlyCount m : mc) {
					CourseSales sales=new CourseSales();
					String targetMonth=m.getSalesMonth();
					Integer calcTuition=tuition*m.getCount();
					Integer calcMaterial=targetMonth.equals(initialMonth) ? material : 0;

					//売り上げマスタへの記録
					sales.setCourseId(courseId);
					sales.setTargetMonth(m.getSalesMonth());
					sales.setMonthlyTuitionFee(tuition*m.getCount());
					sales.setMaterialFee(targetMonth.equals(initialMonth) ? material : 0);
					
					
					//System.out.println(sales);
/*				if(targetMonth.equals(initialMonth)) {
						sales.setMaterialFee(material);
					}else {
						sales.setMaterialFee(0);
					}
*/
					
// test---
					memberMapper.upsertCourseSales(sales);
					System.out.println("途中受講salse---"+sales);
					
					
				//申し込みログ記録(途中申し込み)
//				CourseSalesLog log=new CourseSalesLog();
//				log.setCourseId(courseId);
//				log.setReasonType("REMAINING_APPLICATION");
//				log.setTargetMonth(targetMonth);
//				log.setBeforeTuitionFee(0);
//				log.setBeforeMaterialFee(0);
//				log.setAfterTuitionFee(calcTuition);
//				log.setAfterMaterialFee(calcMaterial);
//				//System.out.println("application log-----"+log);
//				courseSalesLogMapper.insertLog(log);
				
				recordCourseSalesLog
				(courseId,"REMAINING_APPLICATION",targetMonth,0,0,calcTuition,calcMaterial);
				
				}
				//
				for(ClassRoomSchedule c : crs) {
					MemberScheduleStatus scheduleStatus
					=new MemberScheduleStatus();
					scheduleStatus.setMemberId(history.getMemberId());
					scheduleStatus.setCourseId(courseId);
					scheduleStatus.setScheduleId(c.getId());
					scheduleStatus.setStatus("RESERVED");
// test---
					memberMapper.upsertMemberScheduleStatus(scheduleStatus);					
					System.out.println("途中受講status"+scheduleStatus);
				}
				
			// 申込者数の更新
			// 	numberOfApplicant=numberOfApplicant+1をSQL側で処理
			//int numberOfApplicant=ca.getNumberOfApplicant()+1;
				
// test---
				memberMapper.updateApplyedCount(courseId);

						return true;

		}
		// 日程変更による受講マスタの更新//////////////////////
		@Override
		@Transactional
		public void servScheduleChangeCourse(String courseId) {
			
// ////////////////////
			
			//変更前のcourse_salesの取得
			List<CourseSales>beforeList=courseMapper.selectCourseSalesByCourseId(courseId);
			System.out.println("変更前受講料マスタ----"+beforeList); 
			Map<String, CourseSales> beforeMap = new HashMap<>();
			for (CourseSales s : beforeList) {
				beforeMap.put(s.getTargetMonth(), s);
			}
			//変更前の対象月をbeforeMapのkeyから抽出
			List<MonthlyCount>bmc=memberMapper.selectMonthlyCount(courseId);
			Set<String>allMonths=new HashSet<>(beforeMap.keySet());
			for(MonthlyCount m:bmc){
				allMonths.add(m.getSalesMonth());
			}
			System.out.println("変更前受講料マスタmap----"+beforeMap); 
			System.out.println("変更前受講月----"+allMonths); 
// /////////////////////				
			
			
			
			// 講座定員情報の取得 t=申込者数
			CourseCapacity ca=memberMapper.selectByCourseIdForUpdate(courseId)
					.orElseThrow(() -> new IllegalArgumentException("指定された講座が存在しないため処理を中断しました"+courseId));
			Integer t=ca.getNumberOfApplicant();
			// System.out.println("---申込者数="+t);
			// 申込者0のときはcourse_salesへの書き込みをスキップする
			if(t==null || t==0) {
				return;
			}
			
			List<ScheduleAccountingDetail>sad=
					scheduleStatusMapper.selectScheduleAccountingDetails(courseId);
				if(sad.isEmpty()) {
						throw new IllegalStateException("月別回数データが存在しないため処理を中断しました"+courseId);
				}	
				System.out.println("講座変更データ確認----"+sad);
				
			// 年月見出し(＋月ごとの開催回数を取得)開催回数は使用しない
			List<MonthlyCount>mc=memberMapper.selectMonthlyCount(courseId);
				if(mc.isEmpty()) {
						throw new IllegalStateException("月別回数データが存在しないため処理を中断しました"+courseId);
				}	
				System.out.println("講座変更データ確認----"+mc);
				
				// test Listから1個目のデータ抽出
				ScheduleAccountingDetail pFee=sad.get(0);
				Integer paidFee=pFee.getCTuitionFee();
				System.out.println("講座変更確認----"+ paidFee);
								
			// 受講生ごとにStartMonthと支払い済み教材費を取得
/*			Map<String,Integer>materialFee=sad.stream()
 				// 1. 会員ID (chMemberId) ごとに重複を除外し、各会員の代表1件に絞り込む
					.collect(Collectors.toMap(
							ScheduleAccountingDetail::getChMemberId, 
							detail -> detail,
							(existing,replacement)->existing// 同じ会員IDなら最初の1件を保持
			))
					.values().stream()
				// 2. chStartMonth ごとにグループ化し、教材費 (chPaidMaterialFee) を合計する	
					.collect(Collectors.groupingBy(
							ScheduleAccountingDetail::getChStartMonth,
							Collectors.summingInt(ScheduleAccountingDetail::getChPaidMaterialFee)));
*/			
				// 支払い済み教材費 (Key: chStartMonth, Value: chPaidMaterialFee)
				Map<String,Integer>materialFee=new HashMap<>();	
				// 支払い済み受講料 (Key: chCourseId, Value: chPaidTuitionFee)
				Map<String,Integer>tFee=new HashMap<>();	
				//　日程変更後の講座日ごとの受講人数分の受講料を取得
				Map<String,Integer>tuitionFeeByMonth=new HashMap<>();
				
				// 処理済み（計算済み）の会員IDを記録する Set
				Set<Integer>processedMemberIds=new HashSet<>();
				DateTimeFormatter formatter=
						DateTimeFormatter.ofPattern("yyyyMM");				
				// 1回の for ループで全集計を回す
				for(ScheduleAccountingDetail s :sad) {
					Integer memberId=s.getChMemberId();
					// まだ処理していない会員の場合のみ計算を行う
					if(!processedMemberIds.contains(memberId)) {
						// 教材費の集計 (chStartMonth ごと)
						String startMonth=s.getChStartMonth();
						Integer pmFee=s.getChPaidMaterialFee();
						// 該当の startMonth の既存合計額に加算（初回は 0 に加算）
						materialFee.put(
								startMonth, 
								materialFee.getOrDefault(startMonth,0)
								+pmFee);
						// 支払い済み受講料総額の集計 (chCourseId ごと)
						String cId=s.getChCourseId();
						Integer ptFee=s.getChPaidTuitionFee();
					// 該当の startMonth の既存合計額に加算（初回は 0 に加算）
						tFee.put(								
								cId,
								tFee.getOrDefault(cId,0)
								+ptFee);
						// 処理済みとして記録
						processedMemberIds.add(memberId);
					}
						// 【コマごとの処理】会員重複関係なく毎コマ加算
						String targetMonth=s.getCrsDate().format(formatter);
						Integer tuitionFee=s.getCTuitionFee();
				
						tuitionFeeByMonth.put(
						targetMonth, 
						tuitionFeeByMonth.getOrDefault(targetMonth,0)+tuitionFee);					
				}
			
				System.out.println("講座変更教材費確認----"+materialFee);
				System.out.println("講座変更受講料確認----"+tFee);
				System.out.println("講座変更受講料確認----"+tuitionFeeByMonth);
				

				
			
//test用------------			
/*		
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
					sales.setTargetMonth(m.getSalesMonth());
					sales.setMonthlyTuitionFee(tuition*t*m.getCount());
					sales.setMaterialFee
					(targetMonth.equals(initialMonth) ? material*t : 0);
					System.out.println(sales);
				//if(targetMonth.equals(initialMonth)) {
					//	sales.setMaterialFee(material);
					//}else {
					//	sales.setMaterialFee(0);
					//}

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
//						CourseSalesLog log=new CourseSalesLog();
//						log.setCourseId(courseId);
//						log.setReasonType("SCHEDULE_CHANGE");
//						log.setTargetMonth(month);
//						log.setBeforeTuitionFee(beforeTuition);
//						log.setBeforeMaterialFee(beforeMaterial);
//						log.setAfterTuitionFee(afterTuition);
//						log.setAfterMaterialFee(afterMaterial);
//						System.out.println("log-----"+log);
//						courseSalesLogMapper.insertLog(log);
						recordCourseSalesLog
						(courseId,"SCHEDULE_CHANGE",month,beforeTuition,beforeMaterial,afterTuition,afterMaterial);
					}
				}
*/	//検証用コメントアウトここまで			
		
		}
// /////////////////////////////////
		
		
}
