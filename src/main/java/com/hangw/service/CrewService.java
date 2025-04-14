package com.hangw.service;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hangw.model.Crews;
import com.hangw.model.Users;
import com.hangw.repository.CrewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CrewService {
	
	private final CrewRepository crewRepository;
	
	public Crews create(String name, int max, String discription, String category, LocalDate date, LocalDate date2, LocalTime time, String location, Users leader, String imagePath) {
		Crews group = new Crews();
		group.setName(name);
		group.setMax_members(max);
		group.setDescription(discription);
		group.setCategory(category);
		group.setSdate(date);
		group.setEdate(date2);
		group.setTime(time);
		group.setCur_members(1);
		group.setLocation(location);
		group.setLeader(leader);
		group.setImagePath(imagePath);
		
		crewRepository.save(group);
		return group;
	}
	
	public List<Crews> getCrews(int page, int size) {
		Pageable pageable = (Pageable) PageRequest.of(page, size);

		Page<Crews> pageResult = crewRepository.findAllByOrderByCreatedDesc(pageable);
		List<Crews> crewsList = pageResult.getContent();
		return crewsList;
	}
	
	public Crews getCrewByName(String name) {
		Crews crew = crewRepository.findByName(name).get();
		return crew;
	}
	
	public Crews getCrewById(Long id) {
		Crews crew = crewRepository.findById(id).get();
		return crew;
	}
	
	public List<Crews> getCrewsByCategory(String category, int page, int size){
		Pageable pageable = (Pageable) PageRequest.of(page, size);

		Page<Crews> pageResult = crewRepository.getCrewsByCategory(category, pageable);
		List<Crews> crewsList = pageResult.getContent();
		return crewsList;
	}
	
	public List<Crews> getCrewByLeader(Users user){
		return crewRepository.findByLeaderId(user.getId());
	}

	public void save(Crews group) {
		// TODO Auto-generated method stub
		crewRepository.save(group);
	}
}
