package com.example.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.domain.CourseSalesLog;
import com.example.app.service.CourseSalesLogService;

@RestController
@RequestMapping("/api/sales-log")
@CrossOrigin(origins = "http://localhost:5173") // React開発サーバーのURL
public class CourseSalesLogController {

	@Autowired
  private CourseSalesLogService salesLogService;

  // 一覧取得エンドポイント
  @GetMapping
  public List<CourseSalesLog> contSelectCourseSalesLogByCriteria(
  		@RequestParam(required = false) String courseId,
      @RequestParam(required = false) String reasonType,
      @RequestParam(required = false) String targetMonth) {

      return  salesLogService.servSelectCourseSalesLogByCriteria
      		(courseId,reasonType,targetMonth);
  }
  // ドロップダウン選択肢用 API
  @GetMapping("/options")
  public Map<String, Object> getSearchOptions() {
      Map<String, Object> options = new HashMap<>();
      options.put("courseIds", salesLogService.getDistinctCourseIds());
      options.put("reasonTypes", salesLogService.getDistinctReasonTypes());
      options.put("targetMonths", salesLogService.getDistinctTargetMonths());
      return options;
  }
  
}

