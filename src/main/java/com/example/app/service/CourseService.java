package com.example.app.service;

import java.util.List;

import com.example.app.domain.Course;

public interface CourseService {

	//全件
		List<Course> servSelectCourseAll();
	//１件
		Course servSellectCourseById(Integer id);
	//検索

	//登録
		void servInsertCourse(Course course);

	//修正

	//申し込み
		void join(Course course);
	//ページ分割
		List<Course> servSelectCourseByPage(int page, int numPerPage);
		int servSelectTotalPages(int numPerPage);
}
