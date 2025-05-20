package com.hangw.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Entity
@Component
@NoArgsConstructor
public class Crews {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long Id;
	
	@Column(unique = true)
	private String name;
	
	private int max_members;
	
	private int cur_members;
	
	@ManyToOne
	@JoinColumn(name = "leader_id")
	private Users leader;
	
	@Lob
	private String description;
	
	private LocalDate sdate;
	
	private LocalDate edate;
	
	private LocalTime time;
	
	@CreationTimestamp
	private LocalDateTime created;
	
	@ManyToOne
	@JoinColumn(name = "interest_id")
	private Interest category;
	
	private String location;
	
	private String imagePath;
}
