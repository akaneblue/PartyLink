package com.hangw.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hangw.model.Crews;
import com.hangw.model.Schedule;
import com.hangw.repository.CrewRepository;
import com.hangw.repository.ScheduleRepository;
import com.hangw.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

	private final CrewRepository CRepo;
	private final ScheduleRepository SRepo;
	private final UserRepository URepo;
	
	public List<Schedule> getSchedule(Crews crew){
		return SRepo.findScheduleByCrew(crew);
	}
	
	public void save(Schedule schedule) {
		SRepo.save(schedule);
	}
}
