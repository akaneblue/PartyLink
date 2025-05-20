package com.hangw.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
	
	private String imagePath;
	
	@Column(unique = true)
	private String email;
	
	private String name;
	
	private String nickname;
	
	private String password;
	
	private double rating;
	
	private String description;
	
	@ManyToMany
	@JoinTable(
	    name = "user_interest",
	    joinColumns = @JoinColumn(name = "user_id"),
	    inverseJoinColumns = @JoinColumn(name = "interest_id")
	)
	private List<Interest> interest = new ArrayList<>();

	
	private String location;
	
	@Enumerated(EnumType.STRING)
	private Gender gender;
	
	private LocalDate birth;
	
	@ColumnDefault("0")
	private int reviewCount;
	
	public Users(String name, String email) {
		this.name = name;
		this.email = email;
	}
}
