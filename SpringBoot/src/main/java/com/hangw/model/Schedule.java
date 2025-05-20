package com.hangw.model;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Component
@Getter
@Setter
@RequiredArgsConstructor
public class Schedule {

	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "crew_id")
	Crews crew;
	
	private String title;
	private String description;
	
	private LocalDate date;
	
	private LocalTime sTime;
	private LocalTime eTime;
	
	@ManyToOne
	Users writer;
}
