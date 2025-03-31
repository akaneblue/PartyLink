package com.hangw.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Entity
@Component
@NoArgsConstructor
public class Groups {

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
	
	@CreationTimestamp
	private LocalDateTime created;
	
	private String description;
	
	@Enumerated(EnumType.STRING)
	@ColumnDefault("Open")
	private Status status;
	
	private String category;
	
	private String location;
}
