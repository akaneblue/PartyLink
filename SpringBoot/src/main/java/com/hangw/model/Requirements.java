
package com.hangw.model;

import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Component
@Getter
@Setter
@RequiredArgsConstructor
public class Requirements {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@OneToOne
	private Crews crew;
	
	@Enumerated(EnumType.STRING)
	private Gender gender;
	
	private String location;
	
	private Integer minAge;
	
	private Integer maxAge;
}
