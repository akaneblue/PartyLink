package com.hangw.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hangw.model.Crews;
import com.hangw.model.Gender;
import com.hangw.model.Requirements;
import com.hangw.repository.RequirementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequirementService {

	private final RequirementRepository reqRepository;
	
	public Requirements create(Crews crew, Gender gender, Integer minAge, Integer maxAge, String location) {
		Requirements requirement = new Requirements();
		requirement.setCrew(crew);
		requirement.setGender(gender);
		requirement.setMinAge(minAge);
		requirement.setMaxAge(maxAge);
		requirement.setLocation(location);
		reqRepository.save(requirement);
		return requirement;
	}
	
	public Requirements findByCrew(Crews crew) {
		return reqRepository.findByCrew(crew).get(); 
	}
}
