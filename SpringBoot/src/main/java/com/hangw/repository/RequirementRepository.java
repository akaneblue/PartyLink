package com.hangw.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hangw.model.Crews;
import com.hangw.model.Requirements;

@Repository
public interface RequirementRepository extends JpaRepository<Requirements, Long> {
	Optional<Requirements> findByCrew(Crews crew);
}
