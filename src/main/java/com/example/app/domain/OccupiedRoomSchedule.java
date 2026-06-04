package com.example.app.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OccupiedRoomSchedule {

	private List<LocalDate>dateList;// 縦軸：動的に生成された1週間分の日付
	private List<LocalTime>timeList;// 横軸：固定（将来的に動的化可能）の時間枠
	private Map<String,Map<String,Boolean>>occupiedMap;// [部屋名][日付_時間] -> 予約あり(true)/なし(false)
	
}
