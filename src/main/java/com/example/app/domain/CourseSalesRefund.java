package com.example.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseSalesRefund {

		private String courseId;
		private String targetMonth;
		private Integer refundTuitionFee=0;
		private Integer refundMaterialFee=0;
		
		public CourseSalesRefund(String courseId,String targetMonth) {
			this.courseId=courseId;
			this.targetMonth=targetMonth;
		}
		public void addTuitionFee(int fee) {
			this.refundTuitionFee += fee;
		}	
		public void addMaterialFee(int fee) {
			this.refundMaterialFee += fee;
		}
	// コンソール出力用
    @Override
    public String toString() {
        return "CourseSalesRefundDto [" +
                "courseId=" + courseId +
                ", targetMonth='" + targetMonth + '\'' +
                ", refundTuitionFee=" + refundTuitionFee +
                ", refundMaterialFee=" + refundMaterialFee +
                ']';
    }			
			
		
}
