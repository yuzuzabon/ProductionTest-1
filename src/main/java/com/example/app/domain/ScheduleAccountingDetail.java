package com.example.app.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data 
public class ScheduleAccountingDetail {

//MemberScheduleStatus
	private Integer mssId; //
	private String mssStatus;//RESERVED など
	
//ClassRoomSchedule
	private Integer crsId;//ClassRoomSchedule主キー
	private LocalDate crsDate;
	private LocalTime crsStartTime;
	
//CourseHistory
	private Integer chMemberId;//会員番号
	private String chCourseId;//講座番号
	private Integer chPaidTuitionFee;//支払い済み受講料合計
	private Integer chPaidMaterialFee;//支払い済み教材費
	private String chStartMonth;//受講開始月 日程変更後に再計算
	
//Course
	private Integer cTuitionFee; //受講料単価
	private Integer cMaterialFee;//教材費単価
	
}
