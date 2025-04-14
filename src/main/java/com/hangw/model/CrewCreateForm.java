package com.hangw.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrewCreateForm {
	@NotEmpty(message = "이름을 적어주세요.")
	private String name;
	
	@NotEmpty(message = "설명은 작성해주세요.")
	private String description;
	
	@NotNull(message = "날짜를 설정해주세요.")
	private LocalDate date;
	
	private LocalDate date2;
	
	@NotNull(message = "시간을 설정해주세요.")
	private LocalTime time;
	
	private String category;
	
	private String location;
	
	@Min(2)
	private int maxParticipants;
}
