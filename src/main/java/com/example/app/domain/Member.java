package com.example.app.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Member {

		private Integer id;
		private String name;
		private Integer age;
		private String address;
		private Integer typeId;
		private LocalDateTime created;
}
