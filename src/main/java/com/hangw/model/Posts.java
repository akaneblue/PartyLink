package com.hangw.model;

import org.springframework.stereotype.Component;

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
public class Posts {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long Id;

	private long group_id;
	
	private String content;
	
	//private enum status;
	
	private String hashtags;
	
	private String requirements;
}
