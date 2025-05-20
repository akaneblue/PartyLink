package com.hangw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hangw.model.Crews;
import com.hangw.model.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>{
	
	List<Schedule> findScheduleByCrew(Crews crew);
}
