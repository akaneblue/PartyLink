package com.hangw.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hangw.model.Groups;

public interface GroupRepository extends JpaRepository<Groups, Long> {
	Optional<Groups> findByLeaderId(long leader_id);
}
