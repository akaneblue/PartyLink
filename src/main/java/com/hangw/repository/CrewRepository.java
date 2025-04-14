package com.hangw.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hangw.model.Crews;

@Repository
public interface CrewRepository extends JpaRepository<Crews, Long> {
	List<Crews> findByLeaderId(long leader_id);
	
	Optional<Crews> findByName(String name);
	
	Page<Crews> findAllByOrderByCreatedDesc(Pageable pageable);

	Page<Crews> getCrewsByCategory(String category, Pageable pageable);	
}
