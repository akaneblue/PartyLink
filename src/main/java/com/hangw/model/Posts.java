package com.hangw.model;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Component
@NoArgsConstructor
public class Posts {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long Id;
	
	@ManyToOne
	@JoinColumn(name = "group_id")
	private Groups group;
	
	private String content;
	
	@Enumerated(EnumType.STRING)
	@ColumnDefault("Open")
	private Status status;
	
	private String hashtags;
	
	private String requirements;
}
