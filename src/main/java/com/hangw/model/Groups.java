package com.hangw.model;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Component
public class Groups {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long Id;
	
	@Column(unique = true)
	private String name;
	
	private int max_members;
	
	private int cur_members;
	
	private long leader_id;
	
	private String description;
	
	//private enum status;
	
	private String category;
	
	private String location;
}
