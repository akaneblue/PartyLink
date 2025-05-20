package com.hangw.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hangw.model.Interest;

public interface InterestRepository extends JpaRepository<Interest, Long>{

	Optional<Interest> findByName(String name);
}
