package com.hangw.model;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Component
@NoArgsConstructor
public class Users {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long Id;
	
	@Column(unique = true)
	private String email;
	
	private String name;
	
	private String nickname;
	
	private String password;
	
	private double rating;
	
	private String description;
	
	private String interest;
	
	private String location;
	
	public Users(String name, String email) {
		this.name = name;
		this.email = email;
	}
}
