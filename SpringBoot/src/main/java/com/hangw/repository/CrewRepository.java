package com.hangw.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hangw.model.Crews;
import com.hangw.model.Interest;
import com.hangw.model.Users;

@Repository
public interface CrewRepository extends JpaRepository<Crews, Long> {
	List<Crews> findByLeaderId(long leader_id);
	
	Optional<Crews> findByName(String name);
	
	Page<Crews> findAllByOrderByCreatedDesc(Pageable pageable);

	Page<Crews> getCrewsByCategory(Interest category, Pageable pageable);

	Page<Crews> findByLeader(Users leader, Pageable pageable);

	List<Crews> findByNameContainingIgnoreCase(String keyword, Pageable pageable);	
}
